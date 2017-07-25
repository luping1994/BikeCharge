package net.suntrans.bikecharge.api;


import android.graphics.drawable.shapes.OvalShape;

import net.suntrans.bikecharge.bean.CStationResult;
import net.suntrans.bikecharge.bean.ContinueChargeEntity;
import net.suntrans.bikecharge.bean.JanceResult;
import net.suntrans.bikecharge.bean.LoginResult;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.bean.PayObj;
import net.suntrans.bikecharge.bean.PayRecordResult;
import net.suntrans.bikecharge.bean.PushHisResult;
import net.suntrans.bikecharge.bean.QRcodeInfo;
import net.suntrans.bikecharge.bean.ReportListResult;
import net.suntrans.bikecharge.bean.ReportResult;
import net.suntrans.bikecharge.bean.SceneResult;
import net.suntrans.bikecharge.bean.UpLoadImageMessage;
import net.suntrans.bikecharge.bean.UserInfo;
import net.suntrans.bikecharge.bean.UserSetting;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Looney on 2017/1/4.
 */

public interface Api {

    /**
     * 登录api
     *
     * @param grant_type    默认填password
     * @param client_id     默认填6
     * @param client_secret 默认填test
     * @param username      账号
     * @param password      密码
     * @return
     */
    @FormUrlEncoded
    @POST("oauth/login")
    Observable<LoginResult> login(@Field("grant_type") String grant_type,
                                  @Field("client_id") String client_id,
                                  @Field("client_secret") String client_secret,
                                  @Field("username") String username,
                                  @Field("password") String password);

    @POST("user/info")
    Observable<UserInfo> getUserInfo();

    @POST("charge/history")
    Observable<ReportListResult> getHistory();

    @POST("charge/pushHistory")
    Observable<PushHisResult> getPushHis();

    @POST("charge/current")
    Observable<JanceResult> getChargeCurrent();

    @FormUrlEncoded
    @POST("public/code")
    Observable<MSG> getCode(@Field("mobile") String mobile,
                            @Field("type") String type);

    @FormUrlEncoded
    @POST("charge/report")
    Observable<ReportResult> getReportDetail(@Field("report_id") String reportId);

    @FormUrlEncoded
    @POST("public/register")
    Observable<MSG> register(@Field("step") String step,
                             @Field("mobile") String mobile,
                             @Field("code") String code,
                             @Field("password") String password);

    @FormUrlEncoded
    @POST("public/forget")
    Observable<MSG> forget1(@Field("code") String code, @Field("mobile") String mobile, @Field("step") String step);

    @FormUrlEncoded
    @POST("public/forget")
    Observable<MSG> forget2(@Field("step") String step,
                            @Field("mobile") String mobile,
                            @Field("password") String password);


    @POST("bill/index")
    Observable<MSG> getBill();

    @FormUrlEncoded
    @POST("charge/deleteMsg")
    Observable<MSG> deleteChargeMsg(@Field("id") String id);

    @FormUrlEncoded
    @POST("charge/deletePushHistory")
    Observable<MSG> deleteHistory(@Field("id") String id);


    @FormUrlEncoded
    @POST("pay/order")
    Observable<PayObj> getPayObj(@Field("money") String money,
                                 @Field("title") String title,
                                 @Field("pay_code") String pay_code);

    @FormUrlEncoded
    @POST("pay/deposit")
    Observable<PayObj> getVipPayObj(@Field("money") String money,
                                    @Field("title") String title,
                                    @Field("pay_code") String pay_code);

    @Multipart
    @POST("upload/image")
    Observable<UpLoadImageMessage> upload(
            @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("user/auth")
    Observable<String> auth(@Field("truename") String truename,
                            @Field("idcard") String idcard,
                            @Field("sfz_1") String frontId,
                            @Field("sfz_2") String rearId,
                            @Field("car_1") String bikeId);


    @FormUrlEncoded
    @POST("user/profile")
    Observable<MSG> upDateInfo(@FieldMap Map<String, String> map);

    @POST("pay/refund")
    Observable<MSG> payRefund();

    @POST("pay/record")
    Observable<PayRecordResult> getPayRecord();


    @FormUrlEncoded
    @POST("user/bindPush")
    Observable<MSG> bindPush(@Field("registration_id ") String registration_id);

    @POST("charge/sites")
    Observable<CStationResult> getChargeStation();

    @POST("user/setup")
    Observable<UserSetting> getUserSetting();

    @POST
    Observable<QRcodeInfo> getQRcodeInfo(@Url String url);


    @FormUrlEncoded
    @POST("charge/sensus")
    Observable<SceneResult> getSceneInfo(@Field("sensus_id") String sensus_id);

    @POST("charge/continuing")
    Observable<ContinueChargeEntity> continueCharge();

}
