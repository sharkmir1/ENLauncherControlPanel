package kr.ac.snu.hcil.datahalo.notificationdata

import kr.ac.snu.hcil.datahalo.visconfig.AppHaloConfig
import kr.ac.snu.hcil.datahalo.visconfig.KeywordGroupImportancePatterns
import kr.ac.snu.hcil.datahalo.visconfig.NotiProperty
import kr.ac.snu.hcil.datahalo.visconfig.NotificationEnhancementParams
import kotlin.math.roundToLong

abstract class AbstractEnhancedData{
    abstract val typeOfEnhancement: String
    abstract val initTime: Long
    abstract var lifeSpan: Long
}

data class NotiHierarchy(val group:String, val channel:String){
    override fun equals(other: Any?): Boolean = if(other is NotiHierarchy) (other.group == group) && (other.channel == channel) else false
    override fun hashCode(): Int { return super.hashCode() }
}

data class NotiContent (val title:String, val content:String){
    fun contains(s: String):Boolean = title.contains(s) || content.contains(s)
}

//(TODO)notificationGenerator가 있어야 함. id, initTime, 등등 여러 정보 받고, appConfig 세팅 받아서 group 같은 것들도 만들어야됨
/*
*  notification key : cancel 하려면 필요함
*   String title = sbm.getNotification().extras.getString("android.title");
            String text = sbm.getNotification().extras.getString("android.text");
* */

data class EnhancedNotification(
        val id: Int,
        override val typeOfEnhancement: String,
        override val initTime: Long,
        val notiContent: NotiContent = NotiContent("Not Assigned", "Not Assigned")
): AbstractEnhancedData() {

    override var lifeSpan: Long = 0L
    var lifeCycle: EnhancedNotificationLife =
            EnhancedNotificationLife.JUST_TRIGGERED
    var firstPattern = EnhancementPattern.INC
    var secondPattern = EnhancementPattern.INC

    // constant
    var enhanceOffset = 0.0
    var lowerBound = 0.0
    var upperBound = 1.0
    var firstSaturationTime : Long = (lifeSpan.toDouble() * 0.8).roundToLong()
    var secondSaturationTime : Long = (lifeSpan.toDouble() * 0.2).roundToLong()

    // variable
    var slope = 0.0
    var timeElapsed = 0L
    var currEnhancement = enhanceOffset
    var interactionTime : Long = -1 // never interacted : -1, otherwise : time in millis
    var interactionEnhancement = 0.0 // Enhancement Value at the interaction time

    //textInfo
    var keywordGroup: String = KeywordGroupImportancePatterns.ELSE_KEYWORD_GROUP

    var channelHierarchy = NotiHierarchy("Not Assigned", "Not Assigned")

    var whiteRank = 0
    var independent = true

    fun proceedLifeCycleWhenDismiss(){
        if(lifeCycle == EnhancedNotificationLife.JUST_TRIGGERED){
            lifeCycle = EnhancedNotificationLife.JUST_INTERACTED
        }
    }
    
    fun setAppHaloConfig(appHaloConfig: AppHaloConfig){
        val group: String = appHaloConfig.keywordGroupPatterns.assignGroupToNotification(notiContent.title, notiContent.content)
        keywordGroup = group

        val notificationEnhancementParams: NotificationEnhancementParams =
                if(KeywordGroupImportancePatterns.ELSE_KEYWORD_GROUP == group)
                    appHaloConfig.keywordGroupPatterns.getRemainderKeywordGroupPattern().enhancementParam
                else
                    appHaloConfig.keywordGroupPatterns.getEnhancementParamOfGroup(group)!!
        lifeSpan = notificationEnhancementParams.lifespan

        firstPattern = notificationEnhancementParams.firstPattern
        secondPattern = notificationEnhancementParams.secondPattern
        firstSaturationTime = notificationEnhancementParams.firstSaturationTime
        secondSaturationTime = notificationEnhancementParams.secondSaturationTime
        enhanceOffset = notificationEnhancementParams.initialImportance
        currEnhancement = enhanceOffset
        lowerBound = notificationEnhancementParams.importanceRange.first
        upperBound = notificationEnhancementParams.importanceRange.second

    }

    fun getPropertyValue(property: NotiProperty): Any?{
        when(property){
            //NotiProperty.ELAPSED_TIME -> { return timeElapsed}
            NotiProperty.LIFE_STAGE -> {return lifeCycle}
            //NotiProperty.NOTIFICATION_CHANNEL -> {return channelHiearchy}
            NotiProperty.IMPORTANCE -> {return currEnhancement}
            NotiProperty.CONTENT -> {return notiContent}
            else -> {
                return null
            }
        }
    }
}

class EnhancedAppNotifications(val packageName: String) {
    var screenNumber: Int = -1
    var positionInScreen: Pair<Int, Int> = Pair(0, 0)
    var notificationData : MutableList<EnhancedNotification> = mutableListOf()
}