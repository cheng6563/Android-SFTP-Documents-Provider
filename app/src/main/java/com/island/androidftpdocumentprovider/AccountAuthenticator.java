package com.island.androidftpdocumentprovider;
import android.accounts.*;
import android.content.*;
import android.os.*;
import android.util.*;
import java.util.*;
public class AccountAuthenticator extends AbstractAccountAuthenticator
{
	AccountAuthenticator(Context context)
	{
		super(context);
		this.context=context;
	}
	private final Context context;
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,String accountType)
	{
		Log.i(MainActivity.LOG_TAG,"Edit account properties: response="+response+" accountType="+accountType);
		return null;
	}
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,String accountType,String authTokenType,String[]requiredFeatures,Bundle options)
	{
		Log.i(MainActivity.LOG_TAG,"Add account: response="+response+" accountType="+accountType+" authTokenType="+authTokenType+" requiredFeatures="+Arrays.toString(requiredFeatures)+" options="+options);
		Intent intent=new Intent(context,AuthenticationActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,MainActivity.ACCOUNT_TYPE);
		intent.putExtra(AuthenticationActivity.EXTRA_ADD_ACCOUNT,true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,response);
		Bundle bundle=new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT,intent);
		return bundle;
	}
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,Account account,Bundle options)
	{
		Log.i(MainActivity.LOG_TAG,"Confirm account: response="+response+" account="+account+" options="+options);
		return null;
	}
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,Account account,String authTokenType,Bundle options)
	{
		Log.i(MainActivity.LOG_TAG,"Get account token: response="+response+" account="+account+" authTokenType="+authTokenType+" options="+options);
		AccountManager accountManager=AccountManager.get(context);
		String authToken=accountManager.peekAuthToken(account,authTokenType);
		if(authToken==null)
		{
			String password=accountManager.getPassword(account);
			if(password!=null)
			{
				authToken=account.name+"/"+password;
			}
		}
		Bundle result=new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_NAME,account.name);
		result.putString(AccountManager.KEY_ACCOUNT_TYPE,account.type);
		result.putString(AccountManager.KEY_AUTHTOKEN,authToken);
		return result;
	}
	@Override
	public String getAuthTokenLabel(String authTokenType)
	{
		Log.i(MainActivity.LOG_TAG,"Get account token label: authTokenType="+authTokenType);
		return"full";
	}
	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,Account account,String authTokenType,Bundle options)
	{
		Log.i(MainActivity.LOG_TAG,"Update account: response="+response+" account="+account+" authTokenType="+authTokenType+" options="+options);
		return null;
	}
	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,Account account,String[]features)
	{
		Log.i(MainActivity.LOG_TAG,"Account has feature: response="+response+" account="+account+" features="+Arrays.toString(features));
		Bundle result=new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,false);
		return result;
	}
}