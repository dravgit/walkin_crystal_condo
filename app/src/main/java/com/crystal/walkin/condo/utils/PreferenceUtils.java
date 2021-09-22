package com.crystal.walkin.condo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crystal.walkin.R;
import com.crystal.walkin.condo.app.WalkinApplication;
import com.crystal.walkin.condo.models.DepartmentModel;
import com.crystal.walkin.condo.models.ObjectiveTypeModel;
import com.crystal.walkin.condo.models.SignatureModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class PreferenceUtils {

    private static final String PREFERENCE_KEY_TOKEN = "token";
    private static final String PREFERENCE_KEY_LOGIN_USER_NAME = "login_user_name";
    private static final String PREFERENCE_KEY_LOGIN_PASSWORD = "login_password";
    private static final String PREFERENCE_KEY_LOGIN_SUCCESS = "login_success";
    private static final String PREFERENCE_KEY_USER_ID = "user_id";
    private static final String PREFERENCE_KEY_USER_NAME = "user_name";
    private static final String PREFERENCE_KEY_COMPANY_ID = "company_id";
    private static final String PREFERENCE_KEY_COMPANY_NAME = "company_name";
    private static final String PREFERENCE_KEY_COMPANY_ADDRESS = "company_address";
    private static final String PREFERENCE_KEY_COMPANY_PHONE = "company_phone";
    private static final String PREFERENCE_KEY_COMPANY_EMAIL = "company_email";
    private static final String PREFERENCE_KEY_COMPANY_STATUS = "company_status";
    private static final String PREFERENCE_KEY_SIGNATURE = "signature";
    private static final String PREFERENCE_KEY_DEPARTMENT = "department";
    private static final String PREFERENCE_KEY_OBJECTIVE_TYPE = "objective_type";
    private static final String PREFERENCE_KEY_COMPANY_LOGO = "company_logo";
    private static final String PREFERENCE_KEY_COMPANY_NOTE = "company_note";



    private static Context mAppContext;

    // Prevent instantiation
    private PreferenceUtils() {
    }

    public static void init(Context appContext) {
        mAppContext = appContext;
    }

    private static SharedPreferences getSharedPreferences() {
        return mAppContext.getSharedPreferences("walkin", Context.MODE_PRIVATE);
    }

    public static void setUriLogo(String companyLogoUrl) {

        Glide.with(WalkinApplication.appContext)
                .load(companyLogoUrl)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        String path = saveImage(((BitmapDrawable)resource).getBitmap());
                        SharedPreferences.Editor editor = getSharedPreferences().edit();
                        editor.putString(PREFERENCE_KEY_COMPANY_LOGO, path).apply();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    public static Bitmap getBitmapLogo() {
        String path = getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_LOGO, "");
        if (path.isEmpty()) {
            return BitmapFactory.decodeResource(WalkinApplication.appContext.getResources(), R.drawable.print_ogo);
        }
        return getBitmap(path);
    }

    public static void setCompanyNote(String note) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_NOTE, note).apply();
    }

    public static String getCompanyNote() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_NOTE, "");
    }

    public static void setUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static String getUserId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setUserName(String userName) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_NAME, userName).apply();
    }

    public static String getUserName() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_NAME, "");
    }

    public static void setCompanyId(String companyId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_ID, companyId).apply();
    }

    public static String getCompanyId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_ID, "");
    }

    public static void setCompanyName(String companyName) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_NAME, companyName).apply();
    }

    private static String saveImage(Bitmap resource) {

        String savedImagePath = "";
        String imageFileName =  "company_logo" + ".jpg";


        final File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/walkin");

        boolean success = true;
        if(!storageDir.exists()){
            success = storageDir.mkdirs();
        }

        if(success){
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                resource.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return savedImagePath;
    }

    private static Bitmap getBitmap(String path) {
        Bitmap bitmap=null;
        try {
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap ;
    }

    public static String getCompanyName() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_NAME, "");
    }


    public static void setCompanyAddress(String companyAddress) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_ADDRESS, companyAddress).apply();
    }

    public static String getCompanyAddress() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_ADDRESS, "");
    }

    public static void setCompanyPhone(String companyPhone) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_PHONE, companyPhone).apply();
    }

    public static String getCompanyPhone() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_PHONE, "");
    }


    public static void setCompanyEmail(String companyEmail) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_EMAIL, companyEmail).apply();
    }

    public static String getCompanyEmail() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_EMAIL, "");
    }


    public static void setCompanyStatus(String companyStatus) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_COMPANY_STATUS, companyStatus).apply();
    }

    public static String getCompanyStatus() {
        return getSharedPreferences().getString(PREFERENCE_KEY_COMPANY_STATUS, "");
    }

    public static void setSignature(String signature) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_SIGNATURE, signature).apply();
    }

    public static List<SignatureModel> getSignature() {
        String signature = getSharedPreferences().getString(PREFERENCE_KEY_SIGNATURE, "");
        return new Gson().fromJson(signature, new TypeToken<List<SignatureModel>>() {}.getType());
    }

    public static void setDepartment(String department) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_DEPARTMENT, department).apply();
    }

    public static List<DepartmentModel> getDepartment() {
        String department = getSharedPreferences().getString(PREFERENCE_KEY_DEPARTMENT, "");
        return new Gson().fromJson(department, new TypeToken<List<DepartmentModel>>() {}.getType());
    }

    public static void setObjectiveType(String type) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_OBJECTIVE_TYPE, type).apply();
    }

    public static List<ObjectiveTypeModel> getObjectiveType() {
        String objectiveType = getSharedPreferences().getString(PREFERENCE_KEY_OBJECTIVE_TYPE, "");
        return new Gson().fromJson(objectiveType, new TypeToken<List<ObjectiveTypeModel>>() {}.getType());
    }

    public static void setLoginSuccess() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_LOGIN_SUCCESS, true).apply();
    }

    public static void setLoginFail() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_LOGIN_SUCCESS, false).apply();
    }

    public static boolean isLoginSuccess() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_LOGIN_SUCCESS, false);
    }


    public static void setToken(String token) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_TOKEN, token).apply();
    }

    public static String getToken() {
        return getSharedPreferences().getString(PREFERENCE_KEY_TOKEN, "");
    }

    public static void setLoginUserName(String userName) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_LOGIN_USER_NAME, userName).apply();
    }

    public static String getLoginUserName() {
        return getSharedPreferences().getString(PREFERENCE_KEY_LOGIN_USER_NAME, "");
    }

    public static void setLoginPassword(String password) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_LOGIN_PASSWORD, password).apply();
    }

    public static String getLoginPassword() {
        return getSharedPreferences().getString(PREFERENCE_KEY_LOGIN_PASSWORD, "");
    }
}
