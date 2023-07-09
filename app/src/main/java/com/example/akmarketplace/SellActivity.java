package com.example.akmarketplace;

import static android.content.ContentValues.TAG;
import static java.lang.System.currentTimeMillis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class SellActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_Browse2, btn_Sell2, btn_Profile2, btn_Location, btn_Image, btn_EnlistItem;
    private ImageView img_itemDisplay, img_itemImage, img_Default;
    private LatLng loc_meetupLocation;
    //private Item newItem;
    private Toolbar toolbar2;
    private String targetEmail, targetFullname, targetPhone;
    private EditText et_Title, et_Description, et_Price;
    private Uri imageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        btn_Browse2 = findViewById(R.id.btn_Browse2);
        btn_Sell2 = findViewById(R.id.btn_Sell2);
        btn_Profile2 = findViewById(R.id.btn_Profile2);
        btn_Image = findViewById(R.id.btn_Image);
        btn_Location = findViewById(R.id.btn_Location);
        btn_EnlistItem = findViewById(R.id.btn_EnlistItem);
        img_itemDisplay = findViewById(R.id.img_itemDisplay);
        img_Default = new ImageView(getApplicationContext());
        img_Default.setImageDrawable(img_itemDisplay.getDrawable());
        img_itemImage = null;
        loc_meetupLocation = null;
        et_Title = findViewById(R.id.et_Title);
        et_Description = findViewById(R.id.et_Description);
        et_Price = findViewById(R.id.et_Price);

        btn_Browse2.setOnClickListener(this);
        btn_Profile2.setOnClickListener(this);
        btn_Image.setOnClickListener(this);
        btn_Location.setOnClickListener(this);
        btn_EnlistItem.setOnClickListener(this);

        toolbar2 = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, 202);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        targetEmail = user.getEmail();

        DocumentReference docRef = BrowseActivity.db.collection("users").document(targetEmail);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                AccountUser user1 = documentSnapshot.toObject(AccountUser.class);
                targetFullname = user1.getFullname();
                targetPhone = user1.getPhone();
            }
        });


        //targetFullname = user.getDisplayName();
        // = user.getPhoneNumber();

        //newItem = new Item();

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_Location) {
            Intent mapsIntent = new Intent(getApplicationContext(), SetLocActivity.class);
            startActivityForResult(mapsIntent,201);

        }
        else if (v.getId() == R.id.btn_Image) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 101);
        }
        else if (v.getId() == R.id.btn_Browse2) {
            Intent browseIntent = new Intent(getApplicationContext(), BrowseActivity.class);
            startActivity(browseIntent);
            finish();
        }
        else if (v.getId() == R.id.btn_Profile2) {
            Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(profileIntent);
            finish();
        }
        else if (v.getId() == R.id.btn_EnlistItem) {
            boolean checkFields = verifyFields();
            if (checkFields) {
                CollectionReference items = BrowseActivity.db.collection("items");
                Map<String, Object> item = new HashMap<>();
                long timeAdded = currentTimeMillis();
                item.put("time_added_millis", timeAdded); //data type = long
                item.put("title", et_Title.getText().toString());
                item.put("description", et_Description.getText().toString());
                item.put("price", Double.parseDouble(et_Price.getText().toString()));

                StorageReference storeRef = BrowseActivity.storage.getReference().child(et_Title.getText().toString()+(et_Description.getText().toString().length()>7 ? et_Description.getText().toString().substring(0,7) : et_Description.getText().toString()));
                //FirebaseFirestore dbRef = BrowseActivity.db.get
                storeRef.putFile(imageUri);//.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    //@Override
                    //public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //storeRef.getDownloadUrl();//.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            //@Override
                            //public void onSuccess(Uri uri) {
                                item.put("image", storeRef.getDownloadUrl().toString());
                            //}
                        //});
                    //}
                //});

                //item.put("image", imageUri);
                item.put("locationLat", loc_meetupLocation.latitude); //data type = double
                item.put("locationLng", loc_meetupLocation.longitude);

                item.put("sellerName", targetFullname);
                item.put("sellerEmail", targetEmail);
                item.put("sellerPhone", targetPhone);

                item.put("isSold", false);
                item.put("buyerName", "");
                item.put("buyerEmail", "");
                item.put("buyerPhone", "");


                items.document(Long.toString(timeAdded)).set(item);

                Toast.makeText(getApplicationContext(), "Item Successfully Enlisted",
                        Toast.LENGTH_SHORT).show();

                clearFields();
            }
        }
    }


    public boolean verifyFields() {
        if (et_Title.getText().toString().replaceAll(" ", "").isEmpty() ||
                et_Description.getText().toString().replaceAll(" ", "").isEmpty() ||
                img_itemImage == null || loc_meetupLocation == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(SellActivity.this).create();
            alertDialog.setTitle("Populate Fields");

            String message = "The following fields must be populated to enlist the item:\n\n";
            if (et_Title.getText().toString().replaceAll(" ", "").isEmpty()) {
                message = message.concat("Item Title\n");
            }
            if (et_Description.getText().toString().replaceAll(" ", "").isEmpty()) {
                message = message.concat("Item Description\n");
            }
            if (et_Price.getText().toString().replaceAll(" ", "").isEmpty()) {
                message = message.concat("Item Price\n");
            }
            if (img_itemImage == null) {
                message = message.concat("Item Image\n");
            }
            if (loc_meetupLocation == null) {
                message = message.concat("Meetup Location\n");
            }
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return false;
        }
        else return true;
    }

    public void clearFields() {
        et_Title.setText("");
        et_Description.setText("");
        et_Price.setText("");
        img_itemDisplay.setImageDrawable(img_Default.getDrawable());
        img_itemImage = null;
        loc_meetupLocation = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101) {
            Bitmap pic = data.getParcelableExtra("data");
            imageUri = getImageUri(getApplicationContext(), pic);
            img_itemDisplay.setImageURI(imageUri);
            //imageUri = picture;
            img_itemImage = img_itemDisplay;
            Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_SHORT).show();
        }
        else if (requestCode==201) {
            Double locLat = data.getDoubleExtra("replyLat",0);
            Double locLng = data.getDoubleExtra("replyLng",0);
            loc_meetupLocation = new LatLng(locLat,locLng);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }



}