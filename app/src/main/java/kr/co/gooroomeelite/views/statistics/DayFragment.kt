package kr.co.gooroomeelite.views.statistics

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.colorpicker.util.setVisibility
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.adapter.DailySubjectAdapter
import kr.co.gooroomeelite.databinding.FragmentDayBinding
import kr.co.gooroomeelite.viewmodel.SubjectViewModel
import kr.co.gooroomeelite.views.statistics.share.ShareActivity
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


//@RequiresApi(Build.VERSION_CODES.O)
@RequiresApi(Build.VERSION_CODES.Q)
class DayFragment : Fragment() {
    private lateinit var binding: FragmentDayBinding
    private val viewModel: SubjectViewModel by viewModels()
    private val dailySubjectAdapter: DailySubjectAdapter by lazy { DailySubjectAdapter(emptyList()) }
    //아래,왼쪽 제목 이름
    private val ContentColor by lazy {
        ContextCompat.getColor(this.requireContext(), R.color.content_black)
    }
    //그래프 가로 축,선 (점선으로 변경)
    private val transparentBlackColor by lazy {
        ContextCompat.getColor(this.requireContext(), R.color.transparent_black)
    }
    private val listData by lazy {
        mutableListOf(
            //REd, green, blue
            //am 5 ~ 12
            ChartDatas("05:00", arrayListOf(40F)),
            ChartDatas("06:00", arrayListOf(10F, 60F, 40f)),
            ChartDatas("07:00", arrayListOf(0f, 60F, 0f)),
            ChartDatas("08:00", arrayListOf(0f, 0f, 30F)),
            ChartDatas("09:00", arrayListOf(0F, 0f, 10f)),
            ChartDatas("10:00", arrayListOf(0F)),
            ChartDatas("11:00", arrayListOf(0f, 40F)),
            //pm 12 ~ 6
            ChartDatas("12:00", arrayListOf(0f, 50F)),
            ChartDatas("13:00", arrayListOf(0f)),
            ChartDatas("14:00", arrayListOf(0f)),
            ChartDatas("15:00", arrayListOf(0f)),
            ChartDatas("16:00", arrayListOf(0f)),
            ChartDatas("17:00", arrayListOf(0f, 35f, 23f)),
            //pm 18 ~ 24
            ChartDatas("18:00", arrayListOf(0f, 60f)),
            ChartDatas("19:00", arrayListOf(0f, 25f)),
            ChartDatas("20:00", arrayListOf(0f)),
            ChartDatas("21:00", arrayListOf(0f)),
            ChartDatas("22:00", arrayListOf(35f)),
            ChartDatas("23:00", arrayListOf(60f)),
            ChartDatas("24:00", arrayListOf(60f)),
            //am 24 ~ 4
            ChartDatas("01:00", arrayListOf(0f)),
            ChartDatas("02:00", arrayListOf(0f)),
            ChartDatas("03:00", arrayListOf(0f)),
            ChartDatas("04:00", arrayListOf(0f)),
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDayBinding.inflate(inflater, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_day, container, false)
        binding.day = this
        binding.shareButton.setOnClickListener {
            requestPermission()
        }

        binding.dayBarChart.setNoDataText("")

        getTotalStudy()
        moveCalendarByDay()

        initChart(binding.dayBarChart)
        dailySubjectPieChart()
        binding.recyclerViewDay.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = dailySubjectAdapter
        }
        return binding.root
    }

    //오늘 총 공부한 시간
    fun getTotalStudy(){
        viewModel.list.observe(viewLifecycleOwner) {
            //오늘
            val dateNow: LocalDateTime = LocalDateTime.now()
            val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            var todayFormat: String = dateNow.format(textformatter)
            var todaySum: Float = 0f

            //테스트
            val textformatters: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss")
            var todayFormats: String = dateNow.format(textformatters)
            Log.d("serverDateFormat현재시간",todayFormats.toString())

            //어제
            var yesterday: LocalDateTime = dateNow.minusDays(1)
            var yesterdayFormat: String = yesterday.format(textformatter)
            var yesterdaySum: Float = 0f

            var its: Int = 0
            it.forEachIndexed { index, subject ->
                its = it.size
                //서버에서 가져온 요일
                val dateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd")
                val serverDateFormat: String = dateFormat.format(subject.timestamp)
                //테스트 중
                Log.d("serverDateFormat",subject.timestamp.toString())
                Log.d("serverDateFormat",serverDateFormat.toString())
                val dateFormats: DateFormat = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
                val serverDateFormats: String = dateFormats.format(subject.timestamp)
                Log.d("serverDateFormat시간",subject.timestamp.toString())
                Log.d("serverDateFormat시간",serverDateFormats.toString())

                for (it in 0..its) {
                    if (todayFormat == serverDateFormat) {
                        todaySum = subject.studytimeCopy.toFloat()
                        break
                    } else if (yesterdayFormat == serverDateFormat) {
                        yesterdaySum = subject.studytimeCopy.toFloat()
                        break
                    }
                }
            }

            binding.dailyTotalTime.text = "${(todaySum.toInt()) / 60}시간 ${(todaySum.toInt()) % 60}분"

            Log.d("todaytoady", todaySum.toString()) //300
            Log.d("todaytoady", yesterdaySum.toString()) //400

            //지난주와 비교값
            var compareSum: Int = 0
            if ((todaySum/60) > (yesterdaySum/60)) {
                compareSum = todaySum.toInt() - yesterdaySum.toInt() //text
                binding.compareDayTimeImage.setVisibility(true)
                val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                lp.setMargins(20, 15, 0, 0)
                binding.compareDayTimeImage.setLayoutParams(lp)
                binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_up)
                binding.compareDayTimeText.text = "${compareSum / 60}시간"
                binding.compareDayTimeText.setTextColor(Color.parseColor("#F95849"))

            } else if ((todaySum/60) < (yesterdaySum/60)) {
                compareSum = yesterdaySum.toInt() - todaySum.toInt() //text
                binding.compareDayTimeImage.setVisibility(true)
                val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                lp.setMargins(20, 15, 0, 0)
                binding.compareDayTimeImage.setLayoutParams(lp)
                binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_down)
                binding.compareDayTimeText.text = "${compareSum / 60}시간"
                binding.compareDayTimeText.setTextColor(Color.parseColor("#0F8CFF"))
            } else {
                binding.compareDayTimeImage.setImageResource(R.drawable.ic_linezero)
                val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                lp.setMargins(20, 25, 0, 0)
                binding.compareDayTimeImage.setLayoutParams(lp)
                binding.compareDayTimeText.text = "0시간"
                binding.compareDayTimeText.setTextColor(Color.parseColor("#80000000"))
            }
        }

        viewModel.lists.observe(viewLifecycleOwner) {
            //오늘
            val dateNow: LocalDateTime = LocalDateTime.now()
            val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss")
            var todayFormat: String = dateNow.format(textformatter)

            it.forEachIndexed{index, subjects ->
                Log.d("serverDateFormat111",subjects.dayStartTime.toString())
//                Log.d("serverDateFormat",subjects.dayStartTime.toString())
                var dayStartTimeFormat: String? = subjects.dayStartTime?.format(textformatter)
                Log.d("serverDateFormat111",dayStartTimeFormat.toString())

            }
        }
    }

    private fun moveCalendarByDay(){
        val dateNow: LocalDateTime = LocalDateTime.now()
        val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        val titleformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M" + "월 " + "dd" + "일에")

        var count: Int = -1
        var dayCount : Int = 0
        binding.calendar.text = dateNow.format(textformatter) //하루 2021.07.08

        dateNow.plusDays(count.toLong()) //일간탭으로 돌아왔을 때 오늘 날짜로 다시 변경
        binding.calRightBtn.setOnClickListener {
            count++
            if(count == 1) {
                binding.calRightBtn.isEnabled = false
            }else {
                Log.d("countcountRight", count.toString())
                val dayPlus: LocalDateTime = dateNow.plusDays(count.toLong())
                binding.calendar.text = dayPlus.format(textformatter).toString()
                if (count == 0) {
                    binding.titleDay.text = "오늘"
                } else if (count == -1) {
                    binding.calRightBtn.isEnabled = true
                    binding.titleDay.text = "어제"
                } else {
                    binding.titleDay.text = dayPlus.format(titleformatter).toString()
                }
            }
            dayCount++
            viewModel.list.observe(viewLifecycleOwner) {
                //오늘
                val dateNow: LocalDateTime = LocalDateTime.now().plusDays(dayCount.toLong())
                val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                var todayFormat: String = dateNow.format(textformatter)
                var todaySum: Float = 0f

                //어제
                var yesterday: LocalDateTime = dateNow.minusDays(1)
                var yesterdayFormat: String = yesterday.format(textformatter)
                var yesterdaySum: Float = 0f

                var its: Int = 0
                it.forEachIndexed { index, subject ->
                    its = it.size
                    //서버에서 가져온 요일
                    val dateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd")
                    val serverDateFormat: String = dateFormat.format(subject.timestamp)

                    for (it in 0..its) {
                        if (todayFormat == serverDateFormat) {
                            todaySum = subject.studytimeCopy.toFloat()
                            break
                        } else if (yesterdayFormat == serverDateFormat) {
                            yesterdaySum = subject.studytimeCopy.toFloat()
                            break
                        }
                    }
                }

                binding.dailyTotalTime.text = "${(todaySum.toInt()) / 60}시간 ${(todaySum.toInt()) % 60}분"

                Log.d("todaytoady", todaySum.toString()) //300
                Log.d("todaytoady", yesterdaySum.toString()) //400

                //지난주와 비교값
                var compareSum: Int = 0
                if ((todaySum/60) > (yesterdaySum/60)) {
                    compareSum = todaySum.toInt() - yesterdaySum.toInt() //text
                    binding.compareDayTimeImage.setVisibility(true)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 15, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_up)
                    binding.compareDayTimeText.text = "${compareSum / 60}시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#F95849"))

                } else if ((todaySum/60) < (yesterdaySum/60)) {
                    compareSum = yesterdaySum.toInt() - todaySum.toInt() //text
                    binding.compareDayTimeImage.setVisibility(true)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 15, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_down)
                    binding.compareDayTimeText.text = "${compareSum / 60}시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#0F8CFF"))
                } else {
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_linezero)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 25, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeText.text = "0시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#80000000"))
                }
            }
        }


        binding.calLeftBtn.setOnClickListener {
            count--
            Log.d("countcountLeft",count.toString())
            val minusDay: LocalDateTime = dateNow.plusDays(count.toLong())
            binding.calendar.text = minusDay.format(textformatter).toString()
            if (count == 0) {
                binding.titleDay.text = "오늘"
            } else if (count == -1) {
                binding.titleDay.text = "어제"
                binding.calRightBtn.isEnabled = true
            }else {
                binding.titleDay.text = minusDay.format(titleformatter).toString()
            }
            dayCount--
            viewModel.list.observe(viewLifecycleOwner) {
                //오늘
                val dateNow: LocalDateTime = LocalDateTime.now().plusDays(dayCount.toLong())
                val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                var todayFormat: String = dateNow.format(textformatter)
                var todaySum: Float = 0f

                //어제
                var yesterday: LocalDateTime = dateNow.minusDays(1)
                var yesterdayFormat: String = yesterday.format(textformatter)
                var yesterdaySum: Float = 0f

                var its: Int = 0
                it.forEachIndexed { index, subject ->
                    its = it.size
                    //서버에서 가져온 요일
                    val dateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd")
                    val serverDateFormat: String = dateFormat.format(subject.timestamp)

                    for (it in 0..its) {
                        if (todayFormat == serverDateFormat) {
                            todaySum = subject.studytimeCopy.toFloat()
                            break
                        } else if (yesterdayFormat == serverDateFormat) {
                            yesterdaySum = subject.studytimeCopy.toFloat()
                            break
                        }
                    }
                }

                binding.dailyTotalTime.text = "${(todaySum.toInt()) / 60}시간 ${(todaySum.toInt()) % 60}분"

                Log.d("todaytoady", todaySum.toString()) //300
                Log.d("todaytoady", yesterdaySum.toString()) //400

                //지난주와 비교값
                var compareSum: Int = 0
                if ((todaySum/60) > (yesterdaySum/60)) {
                    compareSum = todaySum.toInt() - yesterdaySum.toInt() //text
                    binding.compareDayTimeImage.setVisibility(true)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 15, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_up)
                    binding.compareDayTimeText.text = "${compareSum / 60}시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#F95849"))

                } else if ((todaySum/60) < (yesterdaySum/60)) {
                    compareSum = yesterdaySum.toInt() - todaySum.toInt() //text
                    binding.compareDayTimeImage.setVisibility(true)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 15, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_polygon_down)
                    binding.compareDayTimeText.text = "${compareSum / 60}시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#0F8CFF"))
                } else {
                    binding.compareDayTimeImage.setImageResource(R.drawable.ic_linezero)
                    val lp = LinearLayout.LayoutParams(binding.compareDayTimeImage.getLayoutParams())
                    lp.setMargins(20, 25, 0, 0)
                    binding.compareDayTimeImage.setLayoutParams(lp)
                    binding.compareDayTimeText.text = "0시간"
                    binding.compareDayTimeText.setTextColor(Color.parseColor("#80000000"))
                }
            }
        }
    }

    private fun initChart(chart: BarChart) {
//        customMarkerView.chartView = chart
        with(chart) {
//            marker = customMarkerView
            description.isEnabled = false
            legend.isEnabled = false
            isDoubleTapToZoomEnabled = false

            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(false)
            //둥근 모서리 색상
            val barChartRender = CustomBarChartRender(this, animator, viewPortHandler).apply {
//                setRadius(10)
            }
            renderer = barChartRender
        }
        setData(listData)
    }
    private fun setData(barData: List<ChartDatas>) {
        val values = mutableListOf<BarEntry>()
        barData.forEachIndexed { index, chartData ->
            //첫번째 인자 x , 두번째 인자 y
            for (i in chartData.value) {
                values.add(BarEntry(index.toFloat(), i))
            }
        }

        //막대 그래프 색상 추가
        val barDataSet = BarDataSet(values, "").apply {
            //각 데이터의 값을 텍스트 형식으로 나타내지 않게  (y값 그리기가 활성화되어 있으면 true를 반환하고 그렇지 않으면 false를 반환한다.)
            setDrawValues(false)

            val colors = ArrayList<Int>()
            colors.add(Color.parseColor("#D2EBEE")) //빨
            colors.add(Color.parseColor("#F2A6A0")) //초
            colors.add(Color.parseColor("#CCE99E")) //파
            setColors(colors)
            highLightAlpha = 0
        }

        //막대 그래프 너비 설정
        val dataSets = mutableListOf(barDataSet)
        val data = BarData(dataSets as List<IBarDataSet>?).apply {
            barWidth = 0.5F
        }

        //애니메이션 효과 0.1초
        with(binding.dayBarChart) {
            animateY(1000)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false) //세로선 제거
                textColor = ContentColor
                enableGridDashedLine(5f, 5f, 5f)
                //월 ~ 일
                val xAxisLabels = listOf("05:00", "12:00", "18:00")
                valueFormatter = IndexAxisValueFormatter(xAxisLabels)

            }
            //차트 왼쪽 축, Y방향 ( 수치 최소값,최대값 )
            axisRight.apply {
                textColor = ContentColor
                setDrawAxisLine(false) //격자(일자선
                gridLineWidth = 1F
                gridColor = transparentBlackColor
                axisLineColor = transparentBlackColor //축의 축선 색상
                enableGridDashedLine(5f, 5f, 5f)

                axisMaximum = 60F
                axisMinimum = 0F
                granularity = 20F

                //y축 제목 커스텀
                valueFormatter = object : ValueFormatter() {
                    private val mFormat: DecimalFormat = DecimalFormat("###")
                    override fun getFormattedValue(value: Float): String {
                        return mFormat.format(value) + "분"
                    }
                }
            }

            //차트 오른쪽 축, Y방향 false처리
            axisLeft.apply {
                isEnabled = false
                gridColor = ContentColor

                axisMaximum = 60F
                axisMinimum = 0F
                granularity = 20F
                setLabelCount(4, true) //축 고정간격
            }

            notifyDataSetChanged()
            this.data = data
            invalidate()
        }
    }
    //원 차트
    private fun dailySubjectPieChart() {
        val pieChart: PieChart = binding.dailyPieChart
        pieChart.setUsePercentValues(true)

        viewModel.list.observe(viewLifecycleOwner) {
            val values = mutableListOf<PieEntry>()
            val colorItems = mutableListOf<Int>()
            it.forEach {
                values.add(PieEntry(it.studytime.toFloat(), it.name.toString()))
            }
            it.forEachIndexed { index, subject ->
                colorItems.add(index, Color.parseColor(subject.color))
            }

            val pieDataSet = PieDataSet(values, "")
            pieDataSet.colors = colorItems
            pieDataSet.apply {
//            valueTextColor = Color.BLACK
                setDrawValues(false) //차트에 표시되는 값 지우기
                valueTextSize = 16f
            }
            //% : 퍼센트 수치 색상과 사이즈 지정
            val pieData = PieData(pieDataSet)
            pieChart.apply {
                data = pieData
                description.isEnabled = false //해당 그래프 오른쪽 아래 그래프의 이름을 표시한다.
                isRotationEnabled = false //그래프를 회전판처럼 돌릴 수 있다
//            centerText = "this is color" //그래프 한 가운데 들어갈 텍스트
//            setEntryLabelColor(Color.RED) //그래프 아이템의 이름의 색 지정
                isEnabled = false
                legend.isEnabled = false //범례 지우기
                isDrawHoleEnabled = true //중앙의 흰색 테두리 제거
                holeRadius = 50f //흰색을 증앙에 꽉 채우기
                setDrawEntryLabels(false) //차트에 있는 이름 지우
                animateY(1400, Easing.EaseInOutQuad)
                animate()
            }
        }

    }
    //adapter에 데이터 추가
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.subjectList.observe(viewLifecycleOwner) {
            dailySubjectAdapter.setData(it)
        }
    }
    //사진 권한 허용
    private fun requestPermission(): Boolean {
        var permissions = false
        TedPermission.with(context)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    permissions = true      //p0 = response(응답)
                    val shareIntent = Intent(context, ShareActivity::class.java)
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(shareIntent)
//                    finish()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    permissions = false
                }

            })
            .setDeniedMessage("앱을 실행하려면 권한을 허가하셔야합니다.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
        return permissions
    }
    private fun divideDataFromFirebase() {}
}

//private val listData by lazy {
//    mutableListOf(
//        //REd, green, blue
//        //am 5 ~ 12
//        ChartDatas("05:00", arrayListOf(40F)),
//        ChartDatas("06:00", arrayListOf(10F, 60F, 40f)),
//        ChartDatas("07:00", arrayListOf(0f, 60F, 0f)),
//        ChartDatas("08:00", arrayListOf(0f, 0f, 30F)),
//        ChartDatas("09:00", arrayListOf(0F, 0f, 10f)),
//        ChartDatas("10:00", arrayListOf(0F)),
//        ChartDatas("11:00", arrayListOf(0f, 40F)),
//        //pm 12 ~ 6
//        ChartDatas("12:00", arrayListOf(0f, 50F)),
//        ChartDatas("13:00", arrayListOf(0f)),
//        ChartDatas("14:00", arrayListOf(0f)),
//        ChartDatas("15:00", arrayListOf(0f)),
//        ChartDatas("16:00", arrayListOf(0f)),
//        ChartDatas("17:00", arrayListOf(0f, 35f, 23f)),
//        //pm 18 ~ 24
//        ChartDatas("18:00", arrayListOf(0f, 60f)),
//        ChartDatas("19:00", arrayListOf(0f, 25f)),
//        ChartDatas("20:00", arrayListOf(0f)),
//        ChartDatas("21:00", arrayListOf(0f)),
//        ChartDatas("22:00", arrayListOf(35f)),
//        ChartDatas("23:00", arrayListOf(60f)),
//        ChartDatas("24:00", arrayListOf(60f)),
//        //am 24 ~ 4
//        ChartDatas("01:00", arrayListOf(0f)),
//        ChartDatas("02:00", arrayListOf(0f)),
//        ChartDatas("03:00", arrayListOf(0f)),
//        ChartDatas("04:00", arrayListOf(0f)),
//    )
//}