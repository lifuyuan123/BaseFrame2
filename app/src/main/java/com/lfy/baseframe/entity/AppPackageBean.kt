package com.lfy.baseframe.entity

import android.os.Parcelable
import com.lfy.baseframe.app.clearZero
import com.lfy.baseframe.app.numFormat
import kotlinx.android.parcel.Parcelize

/**
 * @Author admin
 * @Date 2022/10/19-9:52
 * @describe: app更新实体
 */
@Parcelize
data class AppPackageBean(
    val buildAdhocUuids: String,
    val buildBuildVersion: String,
    val buildCates: String,
    val buildCreated: String,
    val buildDescription: String,
    val buildFileSize: String,
    val buildFollowed: String,
    val buildIcon: String,
    val buildIdentifier: String,//包名
    val buildInstallType: String,
    val buildIsAcceptFeedback: String,
    val buildIsFirst: String,
    val buildIsLastest: String,//是否是最新版（1:是; 2:否）
    val buildIsOriginalBuildInHouse: String,
    val buildIsPlaceholder: String,
    val buildIsUploadCrashlog: String,
    val buildKey: String,
    val buildManuallyBlocked: String,
    val buildName: String,//应用名
    val buildPassword: String,
    val buildQRCodeURL: String,
    val buildQrcodeShowAppIcon: String,
    val buildScreenshots: String,
    val buildShortcutUrl: String,
    val buildSignatureType: String,
    val buildTemplate: String,
    val buildType: String,
    val buildUpdateDescription: String,//升级描述
    val buildUpdated: String,//上传更新时间
    val buildVersion: String,//版本名
    val buildVersionNo: String,//版本号
    val buildVersionType: String,
    val canPayDownload: Int,
    val iconUrl: String,
    val isImmediatelyExpired: Boolean,
    val isJoin: Int,
    val isMerged: Int,
    val isOwner: Int,
    val otherApps: List<OtherApp>,
    val otherAppsCount: String,
    val todayDownloadCount: Int
) : Parcelable{
    /**
     * 转换包大小
     */
    fun getSize():String{
        return if (buildFileSize.isNullOrEmpty()) {
            "0M"
        }else{
            "${(buildFileSize.toFloat()/1024/1024).numFormat(2).toFloat().clearZero()}M"
        }
    }
}

@Parcelize
data class OtherApp(
    val buildBuildVersion: String = "",//版本号（蒲公英里的上传版本）
    val buildCreated: String = "",
    val buildIdentifier: String = "",//包名
    val buildKey: String = "",
    val buildName: String = "",//应用名
    val buildUpdateDescription: String = "",//升级描述
    val buildVersion: String = ""//版本名
) : Parcelable