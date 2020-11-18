package com.island.sftp;
import android.net.*;
import android.provider.*;
import android.util.*;
import android.webkit.*;
import com.island.androidsftpdocumentsprovider.provider.*;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.*;
import java.io.*;
import java.net.*;
import java.util.*;
public class SFTP implements Closeable
{
	private static final int TIMEOUT=20000;
	private static final int BUFFER=1024;
	public static final String SCHEME="sftp://";
	public  Uri uri;
	private  String password;
	private  Session session;
	private  ChannelSftp channel;
	private final HashMap<File,Long>lastModified=new HashMap<>();
	private final HashMap<File,Long>size=new HashMap<>();
	private final HashMap<File,Boolean>directory=new HashMap<>();
	private boolean disconnected;
	public SFTP(Uri uri,String password)throws ConnectException
	{
		init(uri, password);
	}

	public SFTP() {
	}

	protected void init(Uri uri, String password) throws ConnectException {
		Log.d(SFTPProvider.TAG,String.format("Created new connection for %s",uri.getAuthority()));
		checkArguments(uri,password);
		this.uri=uri;
		this.password=password;
		JSch jsch=new JSch();
		directory.put(new File("/"),true);
		lastModified.put(new File("/"),0l);
		try
		{
			session=jsch.getSession(uri.getUserInfo(),uri.getHost(),uri.getPort());
			session.setPassword(password);
			Properties config=new Properties();
			config.put("StrictHostKeyChecking","no");
			session.setConfig(config);
			session.setTimeout(TIMEOUT);
			session.setConfig("PreferredAuthentications","password");
			session.connect();
			channel=(ChannelSftp)session.openChannel("sftp");
			channel.connect();
		}
		catch(JSchException e)
		{
			ConnectException exception=new ConnectException(String.format("Can't connect to %s",uri));
			exception.initCause(e);
			throw exception;
		}
	}

	private<T>T getValue(Map<File,T>map,File file)throws IOException
	{
		checkArguments(map,file);
		if(!map.containsKey(file))
		{
			Log.d(SFTPProvider.TAG,"Requested file attributes are unknown");
			listFiles(file.getParentFile());
		}
		if(!map.containsKey(file))throw new FileNotFoundException(String.format("File %s is missing",file));
		return map.get(file);
	}
	public long lastModified(File file)throws IOException
	{
		checkArguments(file);
		return getValue(lastModified,file);
	}
	public long length(File file)throws IOException
	{
		checkArguments(file);
		return getValue(size,file);
	}
	public boolean isDirectory(File file)throws IOException
	{
		checkArguments(file);
		return getValue(directory,file);
	}
	@Override
	public void close() throws IOException
	{
		session.disconnect();
		channel.quit();
		disconnected=true;
	}
	public File[]listFiles(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			Vector vector=channel.ls(file.getPath());
			List<File>files=new ArrayList<>(vector.size()-2);
			for(Object obj:vector)
			{
				LsEntry entry=(LsEntry)obj;
				if(entry.getFilename().equals(".")||entry.getFilename().equals(".."))continue;
				File newFile=new File(file,entry.getFilename());
				SftpATTRS attributes=entry.getAttrs();
				files.add(newFile);
				lastModified.put(newFile,attributes.getMTime()*1000L);
				size.put(newFile,attributes.getSize());
				directory.put(newFile,attributes.isDir());
			}
			return files.toArray(new File[0]);
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public void newFile(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			channel.put(file.getPath()).close();
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public void delete(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			if(isDirectory(file))
			{
				for(File child:listFiles(file))delete(child);
				channel.rmdir(file.getPath());
			}
			else channel.rm(file.getPath());
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public InputStream read(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			return new BufferedInputStream(channel.get(file.getPath()));
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public void mkdirs(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			channel.mkdir(file.getPath());
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public boolean exists(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			getValue(directory,file);
			return true;
		}
		catch(FileNotFoundException e)
		{
			return false;
		}
	}
	public void renameTo(File oldPath,File newPath)throws IOException
	{
		checkArguments(oldPath,newPath);
		try
		{
			channel.rename(oldPath.getPath(),newPath.getPath());
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public OutputStream write(File file)throws IOException
	{
		checkArguments(file);
		try
		{
			return channel.put(file.getPath());
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public static File getFile(Uri uri)
	{
		Objects.requireNonNull(uri);
		return new File(uri.getPath());
	}
	public Uri getUri(File file)
	{
		Objects.requireNonNull(file);
		return Uri.parse(SCHEME+uri.getAuthority()+file.getPath());
	}
	public void get(File from,File to)throws IOException
	{
		checkArguments(from,to);
		try
		{
			Log.d(SFTPProvider.TAG, String.format("Get server file %s to %s", from.getAbsolutePath(), to.getAbsolutePath()));
			channel.get(from.getPath(),to.getPath());
			long lastModified = lastModified(from);
			to.setLastModified(lastModified);
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public void copy(File from,File to)throws IOException
	{
		checkArguments(from,to);
		try
		{
			InputStream input=new BufferedInputStream(channel.get(from.getPath()));
			OutputStream output=new BufferedOutputStream(channel.put(to.getPath()));
			byte[]buffer=new byte[BUFFER];
			while(true)if(write(input,output,buffer)==-1)break;
			input.close();
			output.close();
		}
		catch(SftpException e)
		{
			throw getException(e);
		}
	}
	public String getMimeType(File file)throws IOException
	{
		Objects.requireNonNull(file);
        if(isDirectory(file))
		{
			return DocumentsContract.Document.MIME_TYPE_DIR;
		}
		else
		{
			String name=file.getName();
			int lastDot=name.lastIndexOf('.');
			if(lastDot>=0)
			{
				String extension=name.substring(lastDot+1);
				String mime=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				if(mime!=null)return mime;
			}
			return"application/octet-stream";
		}
    }
	private static int write(InputStream input,OutputStream output,byte[]buffer)throws IOException
	{
		assert input!=null;
		assert output!=null;
		assert buffer!=null;
		int bytesRead=input.read(buffer);
		if(bytesRead!=-1)
		{   
			output.write(buffer,0,bytesRead);
		}
		return bytesRead;
	}
	public static void writeAll(InputStream input,OutputStream output,Observer observer)throws IOException
	{
		Objects.requireNonNull(input);
		Objects.requireNonNull(output);
		input=new BufferedInputStream(input);
		output=new BufferedOutputStream(output);
		byte[]buffer=new byte[SFTP.BUFFER];
		int bytesRead=0;
		long wrote=0;
		while((bytesRead=SFTP.write(input,output,buffer))!=-1)
		{
			wrote+=bytesRead;
			observer.update(null,wrote);
		}
		input.close();
		output.close();
	}
	public static void writeAll(InputStream input,OutputStream output)throws IOException
	{
		writeAll(input,output,null);
	}
	private IOException getException(SftpException cause)
	{
		assert cause!=null;
		if(cause.getCause()!=null)
		{
			SocketException exception=new SocketException("Connection closed");
			exception.initCause(cause);
			return exception;
		}
		else
		{
			ProtocolException exception=new ProtocolException(uri.getScheme());
			exception.initCause(cause);
			return exception;
		}
	}
	private void checkArguments(Object...arguments)
	{
		assert arguments!=null;
		for(Object argument:arguments)Objects.requireNonNull(argument,Arrays.toString(arguments));
		if(disconnected)throw new IllegalStateException("Connection already closed");
	}
}
