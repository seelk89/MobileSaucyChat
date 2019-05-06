package com.example.mobilesaucychat.Shared;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.example.mobilesaucychat.UserPageActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraHelper extends Activity {
    //private String TAG = MainActivity.TAG;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PICTURE_WIDTH = 700;
    private static final int PICTURE_HEIGHT = 700;
    private static final int PICTURE_QUALITY = 90;
    public static final String PICTURE_URI = Variables.PICTURE_URI;
    File takenPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("xyz", "Picture activity was started");

        //open Camera to take new picture
        startCameraActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {

                //scale and rotate taken picture
                Bitmap imageBitmap;
                imageBitmap = scaleAndRotateImage(takenPicture.getPath(), PICTURE_WIDTH, PICTURE_HEIGHT);


                //replace full picture with scaled version
                try {
                    FileOutputStream out = new FileOutputStream(takenPicture);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, PICTURE_QUALITY, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //create new Intent to send picture URI back to the detail Activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(PICTURE_URI, Uri.fromFile(takenPicture).toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            } else if (resultCode == RESULT_CANCELED) {
                //canceled activity
                Log.d("xyz", "camera activity was canceled");
                finish();
            } else {
                Log.d("xyz", " picture was not taken");
                finish();
            }
        }
    }

    //creates empty file (image) in the app private directory
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        takenPicture = image;
        return image;
    }

    //open camera application and saves taken image in apps private directory
    public void startCameraActivity() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("xyz", "file could not be created");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.android.fileprovider",
                        photoFile);

                //save the full picture
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d("xyz", " start picture Activity");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //scale full picture
    private Bitmap scaleAndRotateImage(String imagePath, int imageWidth, int imageHeight) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / imageWidth, photoH / imageHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        //get scaled bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        //get rotated bitmap
        Bitmap rotatedBitmap = rotateScaledBitmap(imagePath, bitmap);

        return rotatedBitmap;
    }

    //rotate scaled picture
    private Bitmap rotateScaledBitmap(String imagePath, Bitmap bitmap) {
        int rotationDegree = 0;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegree = 90;
                Log.d("xyz" , " " + rotationDegree);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegree = 180;
                Log.d("xyz" , " " + rotationDegree);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegree = 270;
                Log.d("xyz" , " " + rotationDegree);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                Log.d("xyz" , " " + rotationDegree);

        }
        //rotate bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;

    }
}