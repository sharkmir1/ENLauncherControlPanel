package kr.ac.snu.hcil.enlaunchercontrolpanel.controlpanel.components.presetselection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class HaloItemDetailsLookup(private val recyclerView: RecyclerView):
        ItemDetailsLookup<Long>(){
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        return recyclerView.findChildViewUnder(e.x, e.y)?.let{ view ->
            (recyclerView.getChildViewHolder(view) as HaloVisComponentAdapter.HaloLayoutViewHolder).getItemDetails()
        }
    }
}