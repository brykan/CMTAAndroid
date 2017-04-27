package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class IssueFragment extends DialogFragment implements View.OnClickListener, OnItemSelectedListener  {

    private static final String TAG = "IssueDetails";
    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private int PICK_IMAGE_REQUEST = 1;
    private int PICK_KITKAT_IMAGE_REQUEST = 2;

    private EditText editIssueLocation;
    private EditText editIssueDetails;
    private Spinner editIssueStatus;
    private ImageView imageView;
    private BitmapDrawable bdrawable;
    private Bitmap bmap;
    public String spinnerItem;
    public String report;
    public boolean okayToDismiss = false;
    private Uri imageUri;
    File photo;
    String imageurl;
    private FloatingActionButton camButton;
    private FloatingActionButton galleryButton;


    private Boolean wantToCloseDialog;
    private byte[] imageData;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createIssueAlert = new AlertDialog.Builder(getActivity());
        createIssueAlert.setTitle("Create Issue");
        Bundle args = getArguments();
        report = args.getString("report_name");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_issue, null);
        //setting the fragment alert view to the view initialized and inflated above
        createIssueAlert.setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        dismiss();
                    }
                });
        //Initializing the EditTexts from above to casts of the cooresponding views in the fragment
        editIssueDetails = (EditText) dialogView.findViewById(R.id.issueDetails);
        editIssueLocation = (EditText) dialogView.findViewById(R.id.issueLocation);
        editIssueStatus = (Spinner)dialogView.findViewById(R.id.spinner);
        editIssueStatus.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.status ,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editIssueStatus.setAdapter(adapter);
        imageView = (ImageView) dialogView.findViewById(R.id.imageView);
        camButton = (FloatingActionButton) dialogView.findViewById(R.id.camButton);
        camButton.setOnClickListener(this);
        galleryButton = (FloatingActionButton) dialogView.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);

        return createIssueAlert.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            d.setCanceledOnTouchOutside(false);

            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    createIssue();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerItem = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public  interface OnCompleteListener {
        void onComplete(Issue issue);
        void onInComplete(Issue issue);
    }
    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    //Getter methods for issue name, number, and location
    public String getIssueLocation() {
        return editIssueLocation.getText().toString();
    }
    public String getIssueDetails() {
        return editIssueDetails.getText().toString();
    }
    public String getIssueStatus() {
        return spinnerItem;
    }

    public void createIssue() {

        final String issueLocation = getIssueLocation();
        final String issueDetails = getIssueDetails();
        final String issueStatus = getIssueStatus();


        if (TextUtils.isEmpty(issueLocation)) {
            Toast.makeText(getActivity(), "Please enter a Issue Location", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(issueDetails)) {
            Toast.makeText(getActivity(), "Please enter Issue Details", Toast.LENGTH_LONG).show();
            return;
        }
        if(okayToDismiss) {
            Issue issue = new Issue(issueLocation, issueStatus, issueDetails, report, imageData, imageData.toString(), bdrawable, bmap);
            Log.d(TAG, "Issue Added with details " + issue.location +", " + issue.status +", " + issue.details);
            wantToCloseDialog = true;
            this.mListener.onComplete(issue);
        }else{
            Issue issue = new Issue(issueLocation,issueStatus,issueDetails,report);
            Log.d(TAG, "Issue Added with details " + issue.location +", " + issue.status +", " + issue.details);
            wantToCloseDialog = true;
            this.mListener.onInComplete(issue);
        }



    }

    public void onClick(View view){
        if(view.getId() == R.id.camButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {

            Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try{
                photo = this.createTemporaryFile("picture",".jpg");
                photo.delete();
            }catch(Exception e)
            {
                Log.v(TAG, "Can't create file to take picture!");
                e.printStackTrace();
                Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
            }
            imageUri = Uri.fromFile(photo);
            camintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (camintent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(camintent,REQUEST_IMAGE_CAPTURE);
            }
        }
        if(view.getId() == R.id.galleryButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {

            Intent galleryintent = new Intent();
            galleryintent.setType("image/*");
            galleryintent.setAction(Intent.ACTION_GET_CONTENT);

            if (Build.VERSION.SDK_INT < 19) {
                startActivityForResult(Intent.createChooser(galleryintent,"Select Picture"),PICK_IMAGE_REQUEST);
            }else{
                startActivityForResult(Intent.createChooser(galleryintent,"Select Picture"),PICK_KITKAT_IMAGE_REQUEST);

            }
        }


    }
    @Override            @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap tempbmap = (Bitmap) extras.get("data");
//            bdrawable = new BitmapDrawable(getResources(),tempbmap);
//            imageView.setBackground(bdrawable);
//            encodeBitmap(bmap);
            this.grabImage(imageView);
            okayToDismiss = true;
        }else if(requestCode == PICK_KITKAT_IMAGE_REQUEST && resultCode == RESULT_OK){
            try {
                imageUri = data.getData();
                String pathsegment[] = imageUri.getLastPathSegment().split(":");
                String id = pathsegment[1];
                final String[] projection = {MediaStore.Images.Media.DATA};

                Uri uri = getUri();
                String selectedPath = "path";
                Cursor cursor = getActivity().getContentResolver().query(uri, projection, MediaStore.Images.Media._ID +"="+id, null, null);
                if (cursor.moveToFirst()){
                    selectedPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    imageUri = Uri.fromFile(new File(selectedPath));
                }
                grabImage(imageView);
                okayToDismiss = true;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    private Uri getUri(){
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }
    public void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,25,baos);
        imageData = baos.toByteArray();
    }
    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
    public void grabImage(ImageView imageView)
    {
        getActivity().getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getActivity().getContentResolver();
        try
        {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 8;
            //InputStream stream = cr.openInputStream(imageUri);
            //bmap = BitmapFactory.decodeStream(stream,null,options);
            bmap = handleSamplingAndRotationBitmap(getActivity(),imageUri);
            encodeBitmap(bmap);
            Glide.with(getActivity()).load(imageData).into(imageView);
            //imageView.setImageBitmap(thumbImage);
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.d(TAG, "Failed to load", e);
        }
    }
    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 256;
        int MAX_WIDTH = 256;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)*4;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }
    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 4;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}
