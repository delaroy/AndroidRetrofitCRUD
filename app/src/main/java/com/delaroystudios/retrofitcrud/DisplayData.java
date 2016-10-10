package com.delaroystudios.retrofitcrud;

/**
 * Created by delaroystudios on 10/5/2016.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.delaroystudios.retrofitcrud.helper.AppConfig;
import com.delaroystudios.retrofitcrud.helper.ListViewAdapter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DisplayData extends AppCompatActivity {

    String BASE_URL = "http://www.delaroystudios.com";

    ListView details_list;
    ListViewAdapter displayAdapter;
    ArrayList<String> id = new ArrayList<>();
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> age = new ArrayList<>();
    ArrayList<String> mobile = new ArrayList<>();
    ArrayList<String> email = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        details_list = (ListView) findViewById(R.id.retrieve);
        displayAdapter = new ListViewAdapter(getApplicationContext(), id, name, age, mobile, email);
        displayData();

        details_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long ids) {
                Intent i = new Intent(getApplicationContext(),UpdateData.class);
                i.putExtra("id",id.get(position));
                i.putExtra("name",name.get(position));
                i.putExtra("age",age.get(position));
                i.putExtra("mobile",mobile.get(position));
                i.putExtra("email", email.get(position));
                startActivity(i);

            }
        });

        details_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long ids) {
                delete(id.get(position));
                return true;
            }
        });
    }


    public void displayData() {

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL) //Setting the Root URL
                .build();

        AppConfig.read api = adapter.create(AppConfig.read.class);

        api.readData(new Callback<JsonElement>() {
                         @Override
                         public void success(JsonElement result, Response response) {

                             String myResponse = result.toString();
                             Log.d("response", "" + myResponse);

                             try {
                                 JSONObject jObj = new JSONObject(myResponse);

                                 int success = jObj.getInt("success");

                                 if (success == 1) {

                                     JSONArray jsonArray = jObj.getJSONArray("details");
                                     for (int i = 0; i < jsonArray.length(); i++) {

                                         JSONObject jo = jsonArray.getJSONObject(i);

                                         id.add(jo.getString("id"));
                                         name.add(jo.getString("name"));
                                         age.add(jo.getString("age"));
                                         mobile.add(jo.getString("mobile"));
                                         email.add(jo.getString("email"));

                                     }

                                     details_list.setAdapter(displayAdapter);

                                 } else {
                                     Toast.makeText(getApplicationContext(), "No Details Found", Toast.LENGTH_SHORT).show();
                                 }
                             } catch (JSONException e) {
                                 Log.d("exception", e.toString());
                             }
                         }

                         @Override
                         public void failure(RetrofitError error) {
                             Log.d("Failure", error.toString());
                             Toast.makeText(DisplayData.this, error.toString(), Toast.LENGTH_LONG).show();
                         }
                     }
        );
    }

    public void delete(String id){

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL) //Setting the Root URL
                .build();

        AppConfig.delete api = adapter.create(AppConfig.delete.class);

        api.deleteData(
                id,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {

                        try {

                            BufferedReader reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            String resp;
                            resp = reader.readLine();
                            Log.d("success", "" + resp);

                            JSONObject jObj = new JSONObject(resp);
                            int success = jObj.getInt("success");

                            if(success == 1){
                                Toast.makeText(getApplicationContext(), "Successfully deleted", Toast.LENGTH_SHORT).show();
                                recreate();
                            } else{
                                Toast.makeText(getApplicationContext(), "Deletion Failed", Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            Log.d("Exception", e.toString());
                        } catch (JSONException e) {
                            Log.d("JsonException", e.toString());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(DisplayData.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

}

