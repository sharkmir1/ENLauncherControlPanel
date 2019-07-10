package kr.ac.snu.hcil.datahalo.haloview

import android.content.Context
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import kr.ac.snu.hcil.datahalo.notificationdata.EnhancedAppNotifications
import kr.ac.snu.hcil.datahalo.visconfig.AppHaloConfig
import kr.ac.snu.hcil.datahalo.visconfig.NotificationType
import kr.ac.snu.hcil.datahalo.manager.VisDataManager
import kr.ac.snu.hcil.datahalo.visualEffects.AbstractIndependentVisEffect
import kr.ac.snu.hcil.datahalo.manager.AppHaloLayoutMethods
import kr.ac.snu.hcil.datahalo.manager.VisEffectManager
import kr.ac.snu.hcil.datahalo.visualEffects.AbstractAggregatedVisEffect

class AppNotificationHalo(context: Context, attributeSet: AttributeSet? = null)
    : ConstraintLayout(context, attributeSet){

    companion object {
        const val TAG = "APP_NOTIFICATION_HALO"
    }

    private var visConfig: AppHaloConfig? = null
    private var appPackageName: String = "Not Initialized"

    private val pivotViewID: Int

    private val currentIndependentNotificationIDs: MutableList<Int> = mutableListOf()
    //private val currentAggregatedNotificationIDs : MutableList<Int> = mutableListOf()
    private val currentIndependentNotiVisLayoutInfos: MutableMap<Int, LayoutParams> = mutableMapOf()
    private val currentIndependentVisEffects: MutableMap<Int, AbstractIndependentVisEffect> = mutableMapOf()
    private var currentAggregatedVisEffect: AbstractAggregatedVisEffect? = null

    init{
        clipChildren = false
        clipToPadding = false
        clipToOutline = false

        //setBackgroundColor(Color.LTGRAY)

        id = View.generateViewId()

        pivotViewID = View.generateViewId()

        val pivotView = ImageView(context).also{
            it.id = pivotViewID
            it.setBackgroundColor(Color.RED)
        }

        addView(pivotView)

        ConstraintSet().apply{
            clone(this@AppNotificationHalo)

            constrainHeight(pivotView.id, 10)
            constrainWidth(pivotView.id, 10)

            connect(pivotView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(pivotView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
            connect(pivotView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(pivotView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

            applyTo(this@AppNotificationHalo)
        }
    }

    /* 필요 기능
     *  1) independent, aggregated Notification ID 관리
     *  2) independent, aggregated Notification과 매핑되는 visEffect를 관리
     *  3) independent, aggregated VisEffect의 위치 관리
     *  4) independent, aggregated VisEffect에 공히 적용되는 animation 효과
     *  5) App Notification Data 들어오면 각 시각화에 전파
     */

    fun getAppPackageName(): String = appPackageName

    fun setVisConfig(appHaloConfig: AppHaloConfig){
        visConfig = appHaloConfig
        appPackageName = appHaloConfig.packageName

        //parameter updates
        currentIndependentNotiVisLayoutInfos.forEach{
            //TODO()
        }
        currentIndependentVisEffects.values.forEachIndexed{index, effect ->
            effect.visualParameters = appHaloConfig.independentVisEffectVisParams
            effect.independentVisObjects.forEachIndexed{ index, visObj ->
                visObj.setVisParams(appHaloConfig.independentVisualParameters[index])
                visObj.setDataParams(appHaloConfig.independentDataParameters[index])
                visObj.setAnimParams(*appHaloConfig.independentAnimationParameters[index].toTypedArray())
                visObj.setVisMapping(appHaloConfig.independentVisualMappings[index])
            }
        }
        currentAggregatedVisEffect

    }

    fun setAppHaloData(enhancedAppNotifications: EnhancedAppNotifications){
        //관련 없는 app의 notiData면 끝

        if(enhancedAppNotifications.packageName != appPackageName)
            return

        visConfig?.let{config ->
            val result = VisDataManager.convert(enhancedAppNotifications, config)
            val independentNotis = result[NotificationType.INDEPENDENT]?: emptyList()
            val aggregatedNotis = result[NotificationType.AGGREGATED]?: emptyList()

            //update, add, delete 필요함

            //0. Delete Phase
            currentIndependentNotificationIDs.filter{currentlyValidID -> currentlyValidID !in independentNotis.map{it.id}}.map{ expiredID ->
                val visEffect = currentIndependentVisEffects[expiredID]!!
                visEffect.deleteVisObjectsInLayout(this)

                currentIndependentNotificationIDs.remove(expiredID)
                currentIndependentNotiVisLayoutInfos.remove(expiredID)
                currentIndependentVisEffects.remove(expiredID)
            }

            // 1. Data Update Phase

            //1-1. update (기존에 존재하는 애들을 업데이트 하는 방식임
            val updateIndependent = independentNotis.filter{it.id in currentIndependentNotificationIDs}
            updateIndependent.map{ enhancedNotification ->
                currentIndependentVisEffects[enhancedNotification.id]?.let{ visEffect ->
                    visEffect.setEnhancedNotification(enhancedNotification)
                }
            }

            //1-2. add
            val addIndependent = independentNotis.filterNot{it.id in currentIndependentNotificationIDs}
            addIndependent.map{ enhancedNotification ->
                val visEffect = VisEffectManager.createNewIndependentVisEffect(config.independentVisEffectName, config)

                currentIndependentNotificationIDs.add(enhancedNotification.id)
                currentIndependentVisEffects[enhancedNotification.id] = visEffect
                visEffect.setEnhancedNotification(enhancedNotification)
            }

            val (independentVisLayoutMap, aggregatedVisLayout) = AppHaloLayoutMethods
                    .getLayout(config)
                    .generateLayoutParams(this, pivotViewID, independentNotis, currentIndependentVisEffects.toMap(), aggregatedNotis, currentAggregatedVisEffect)

            // 2. Layout Update Phase
            currentIndependentVisEffects.forEach{ idToEffect ->
                val notiID = idToEffect.key
                val visEffect = idToEffect.value

                independentVisLayoutMap[notiID]?.let{ layout ->
                    currentIndependentNotiVisLayoutInfos[notiID] = layout
                    visEffect.placeVisObjectsInLayout(this, layout)
                }
            }

            //TODO(aggregated effect)
            currentAggregatedVisEffect.let{

            }
        }
    }

}