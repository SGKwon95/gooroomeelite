package kr.co.gooroomeelite.views.statistics

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.views.statistics.share.ShareActivity

class StatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val pageView = inflater.inflate(R.layout.fragment_statistics, container, false)
        val tabs: TabLayout = pageView.findViewById(R.id.tabs)

        val dayFragment = DayFragment()
        val weekFragment = WeekFragment()
        val monthFragment = MonthFragment()

        parentFragmentManager.beginTransaction().add(R.id.chart_container, dayFragment).commit()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab!!.position

                var selected: Fragment? = null
                if (position == 0) {
                    selected = dayFragment
                } else if (position == 1) {
                    selected = weekFragment
                } else if (position == 2) {
                    selected = monthFragment
                }
                if (selected != null) {
                    parentFragmentManager.beginTransaction().replace(R.id.chart_container, selected)
                        .commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        
        val shareButton : Button = pageView.findViewById(R.id.share_button)
        shareButton.setOnClickListener{
            requestPermission()
        }
        return pageView
    }

    private fun requestPermission(): Boolean{
        var permissions = false
        TedPermission.with(context)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    permissions = true      //p0=response(??????)
                    val shareIntent = Intent(context,ShareActivity::class.java)
                    startActivity(shareIntent)
//                    finish()
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    permissions = false
                }

            })
            .setDeniedMessage("?????? ??????????????? ????????? ????????????????????????.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
        return permissions
    }
}

