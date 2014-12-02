package com.example.campuseventsapp;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetupLoginInfo extends Activity {


	EditText username, password;
	TextView organization;
	Button submit;
	AdminAccounts newAccount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_login_info);

		newAccount = new AdminAccounts();
		FillAutoCompleteView();
		
		username = (EditText)findViewById(R.id.user);
		password = (EditText)findViewById(R.id.pw);
		organization = (TextView)findViewById(R.id.orgName);
		submit = (Button)findViewById(R.id.submitRequest);

		submit.setOnClickListener(new OnClickListener() {	
			
			@Override
			public void onClick(View v) {
				if (username.getText().toString().isEmpty()
						|| password.getText().toString().isEmpty()
						|| organization.getText().toString().isEmpty()) {
					Toast.makeText(getApplicationContext(),"Missing fields", Toast.LENGTH_LONG).show();
				} else {
					
					newAccount.setPassword(password.getText().toString());
					newAccount.setUsername(username.getText().toString());
					newAccount.setOrganizationName(organization.getText().toString());
					
					String x = organization.getText().toString();
					//query for all things and to see if this username currently exists, if doesnt add, if does, do toast
					ParseQuery<AdminAccounts> query = ParseQuery.getQuery(AdminAccounts.class);
					query.whereEqualTo("organizationName", x);
					query.findInBackground(new FindCallback<AdminAccounts> () {

						@Override
						public void done(List<AdminAccounts> arg0, ParseException arg1) {
							
							if (arg1 == null && arg0.size() > 0) {
								
								Toast.makeText(getApplicationContext(), "This organization has an account", Toast.LENGTH_LONG).show();
								
							} else {
								Toast.makeText(getApplicationContext(), "Account created successfully! :) ", Toast.LENGTH_LONG).show();
								newAccount.saveInBackground();
								setResult(Activity.RESULT_OK);
								finish();
							}
							
						}
						
					});
				}
			}

			
		});
	}

	
	private void FillAutoCompleteView() {
		/* ***********************************************************************************
		 * This Query populates the AutoCompleteTextView with all the Buildings on Campus
		 * in a background thread. 
		 * ***********************************************************************************
		 */
		ParseQuery<UMDBuildings> query = ParseQuery.getQuery(UMDBuildings.class);
		query.whereExists("name"); // will get all the buildings
		query.setLimit(200);       // setting max num of queries
		query.findInBackground(new FindCallback<UMDBuildings>() {

			@Override
			public void done(List<UMDBuildings> arg0, ParseException arg1) {
				ArrayList<String> buildNames = new ArrayList<String>(arg0.size());

				for (UMDBuildings a: arg0) {
					buildNames.add(a.getName());
				}

				AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.orgName);

				// Create an ArrayAdapter containing country names
				ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(SetupLoginInfo.this,
						R.layout.list_item, buildNames.toArray());

				textView.setAdapter(adapter);
			}			
		});
	}


}
