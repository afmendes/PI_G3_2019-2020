package com.example.projetopi

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    // User Authentication
    private lateinit var auth: FirebaseAuth
    // Database reference
    private lateinit var ref: DatabaseReference
    // Buttons for logout and password update
    private lateinit var logoutBtn: Button
    private lateinit var updatePass: Button
    // Buttons for time
    private lateinit var hourBtn: Button
    private lateinit var dayBtn: Button
    private lateinit var monthBtn: Button
    // Switches
    private lateinit var airHumSw: Switch
    private lateinit var batChargeSw: Switch
    private lateinit var lumSw: Switch
    private lateinit var soilHumSw: Switch
    private lateinit var tempSw: Switch
    // Line Plot
    private lateinit var lchart: LineChart
    // Flags for the graphic display
    private var flagAirHum = false
    private var flagBatCharge = false
    private var flagLum = false
    private var flagSoilHum = false
    private var flagTemp = false
    // Flags for the values existence
    private var airHumExists = false
    private var batChargeExists = false
    private var lumExists = false
    private var soilHumExists = false
    private var tempExists = false

    // Setup for the graphic data sets
    private lateinit var xAxis: XAxis


    val timeArray = ArrayList<String>()

    var timeFormType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference("users/" + auth.currentUser!!.uid)

        if(auth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_main)

        logoutBtn = findViewById(R.id.logout_btn)
        updatePass = findViewById(R.id.update_pass_btn)


        hourBtn = findViewById(R.id.hour_btn)
        dayBtn = findViewById(R.id.day_btn)
        monthBtn = findViewById(R.id.month_btn)

        airHumSw = findViewById(R.id.air_hum_sw)
        batChargeSw = findViewById(R.id.bat_charge_sw)
        lumSw = findViewById(R.id.lum_sw)
        soilHumSw = findViewById(R.id.soil_hum_sw)
        tempSw = findViewById(R.id.temp_sw)

        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        updatePass.setOnClickListener{
            val intent = Intent(this, UpdatePassword::class.java)
            startActivity(intent)
        }

        // TODO: Meter botoes a fazer as médias

        hourBtn.setOnClickListener{
            timeFormType = 0
            xAxis.valueFormatter = MyValueFormatter(timeFormType, timeArray)
            lchart.invalidate() // refresh
        }

        dayBtn.setOnClickListener{
            timeFormType = 1
            xAxis.valueFormatter = MyValueFormatter(timeFormType, timeArray)
            lchart.invalidate() // refresh
        }

        monthBtn.setOnClickListener{
            timeFormType = 2
            xAxis.valueFormatter = MyValueFormatter(timeFormType, timeArray)
            lchart.invalidate() // refresh
        }

        airHumSw.setOnClickListener {
            if(airHumExists) {
                if (flagAirHum) {
                    ref.child("flags").child("airHumidity").setValue(0)
                    airHumSw.isChecked = false
                } else {
                    ref.child("flags").child("airHumidity").setValue(1)
                    airHumSw.isChecked = true
                }
            }
        }

        batChargeSw.setOnClickListener{
            if(batChargeExists) {
                if (flagBatCharge) {
                    ref.child("flags").child("batteryCharge").setValue(0)
                    batChargeSw.isChecked = false
                } else {
                    ref.child("flags").child("batteryCharge").setValue(1)
                    batChargeSw.isChecked = true
                }
            }
        }

        lumSw.setOnClickListener{
            if(lumExists){
                if(flagLum){
                    ref.child("flags").child("luminosity").setValue(0)
                    lumSw.isChecked = false
                } else{
                    ref.child("flags").child("luminosity").setValue(1)
                    lumSw.isChecked = true
                }
            }
        }

        soilHumSw.setOnClickListener{
            if(soilHumExists){
                if(flagSoilHum){
                    ref.child("flags").child("soilHumidity").setValue(0)
                    soilHumSw.isChecked = false
                } else {
                    ref.child("flags").child("soilHumidity").setValue(1)
                    soilHumSw.isChecked = true
                }
            }
        }

        tempSw.setOnClickListener{
            if(tempExists){
                if(flagTemp){
                    ref.child("flags").child("temperature").setValue(0)
                    tempSw.isChecked = false
                } else {
                    ref.child("flags").child("temperature").setValue(1)
                    tempSw.isChecked = true
                }
            }
        }





        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                error("Couldn't connect to database")
            }

            override fun onDataChange(p0: DataSnapshot) {


                val airHumEntries =  ArrayList<Entry>()
                val batChargeEntries = ArrayList<Entry>()
                val lumEntries =  ArrayList<Entry>()
                val soilHumEntries = ArrayList<Entry>()
                val tempEntries = ArrayList<Entry>()

                lchart = findViewById<View>(R.id.chart) as LineChart
                xAxis = lchart.xAxis

                airHumExists = false
                batChargeExists = false
                lumExists = false
                soilHumExists = false
                tempExists = false

                if( p0.exists() ){
                    if(p0.child("exists").value.toString() == "1") {
                        flagAirHum = (p0.child("flags").child("airHumidity").value.toString().toInt() == 1)
                        flagBatCharge = (p0.child("flags").child("batteryCharge").value.toString().toInt() == 1)
                        flagLum = (p0.child("flags").child("luminosity").value.toString().toInt() == 1)
                        flagSoilHum = (p0.child("flags").child("soilHumidity").value.toString().toInt() == 1)
                        flagTemp = (p0.child("flags").child("temperature").value.toString().toInt() == 1)

                        for (h in p0.children) {
                            timeArray.clear()
                            if (h.key.equals("airHumidity")) {
                                airHumExists = true
                                if (flagAirHum) {
                                    for ((index, k) in h.children.withIndex()) {
                                        airHumEntries.add(
                                            Entry(
                                                index.toFloat(),
                                                k.value.toString().toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                            if (h.key.equals("batteryCharge")) {
                                batChargeExists = true
                                if (flagBatCharge) {
                                    for ((index, k) in h.children.withIndex()) {
                                        batChargeEntries.add(
                                            Entry(
                                                index.toFloat(),
                                                k.value.toString().toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                            if (h.key.equals("luminosity")) {
                                lumExists = true
                                if (flagLum) {
                                    for ((index, k) in h.children.withIndex()) {
                                        lumEntries.add(
                                            Entry(
                                                index.toFloat(),
                                                k.value.toString().toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                            if (h.key.equals("soilHumidity")) {
                                soilHumExists = true
                                if (flagSoilHum) {
                                    for ((index, k) in h.children.withIndex()) {
                                        soilHumEntries.add(
                                            Entry(
                                                index.toFloat(),
                                                k.value.toString().toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                            if (h.key.equals("temperature")) {
                                tempExists = true
                                if (flagTemp) {
                                    for ((index, k) in h.children.withIndex()) {
                                        tempEntries.add(
                                            Entry(
                                                index.toFloat(),
                                                k.value.toString().toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                            if (h.key.equals("time")) {
                                for (k in h.children) {
                                    val str = k.value.toString()
                                    timeArray.add(str)
                                }
                            }

                        }

                        airHumSw.isClickable = true
                        batChargeSw.isClickable = true
                        lumSw.isClickable = true
                        soilHumSw.isClickable = true
                        tempSw.isClickable = true

                        /*
                        airHumSw.visibility = View.VISIBLE
                        batChargeSw.visibility = View.VISIBLE
                        lumSw.visibility = View.VISIBLE
                        soilHumSw.visibility = View.VISIBLE
                        tempSw.visibility = View.VISIBLE
                        */

                        if(airHumExists && flagAirHum){
                            airHumSw.isChecked = true
                        } else if (!airHumExists){
                            ref.child("flags").child("airHumidity").setValue(0)
                            airHumSw.isChecked = false
                            airHumSw.isClickable = false
                            //airHumSw.visibility = View.INVISIBLE
                        }

                        if(batChargeExists && flagBatCharge){
                            batChargeSw.isChecked = true
                        } else if (!batChargeExists){
                            ref.child("flags").child("batteryCharge").setValue(0)
                            batChargeSw.isChecked = false
                            batChargeSw.isClickable = false
                            //batChargeSw.visibility = View.INVISIBLE
                        }

                        if(lumExists && flagLum){
                            lumSw.isChecked = true
                        } else if (!lumExists){
                            ref.child("flags").child("luminosity").setValue(0)
                            lumSw.isChecked = false
                            lumSw.isClickable = false
                            //lumSw.visibility = View.INVISIBLE
                        }

                        if(soilHumExists && flagSoilHum){
                            soilHumSw.isChecked = true
                        } else if (!soilHumExists){
                            ref.child("flags").child("soilHumidity").setValue(0)
                            soilHumSw.isChecked = false
                            soilHumSw.isClickable = false
                            //soilHumSw.visibility = View.INVISIBLE
                        }

                        if(tempExists && flagTemp){
                            tempSw.isChecked = true
                        } else if (!tempExists) {
                            ref.child("flags").child("temperature").setValue(0)
                            tempSw.isChecked = false
                            tempSw.isClickable = false
                            //tempSw.visibility = View.INVISIBLE
                        }
                        updatePlot(airHumEntries,batChargeEntries,lumEntries,soilHumEntries,tempEntries,
                            timeArray, timeFormType)

                    }else{
                        val intent = Intent(this@MainActivity, AddDeviceActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }
            }

        })
    }
    //TODO: Adicionar a lógica para cálculo de médias e total para dias e meses
    /*
    type:
        0 - Hours   -- 06h32
        1 - Daily   -- 22/May
        2 - Monthly -- May/2020
     */
    class MyValueFormatter(private val type: Int , private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toString()
        }

        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            val xDataFormatted = ArrayList<String>()
            for (k in xValsDateLabel){
                //2018-05-28T16:00:13Z
                val year = k.substring(0,4)
                val monthInt = k.substring(5,7)
                val day = k.substring(8,10)
                val hour = k.substring(11,13)
                val minutes = k.substring(14,16)

                val month = when(monthInt.toInt()){
                    1 -> "Jan"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Apr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Aug"
                    9 -> "Sep"
                    10 -> "Oct"
                    11 -> "Nov"
                    else -> "Dec"
                }

                val xValsDateLabelFormatted = when(type){
                    0 -> "${hour}h${minutes}"
                    1 -> "${day}/${month}"
                    2 -> "${month}/${year}"
                    else -> "Error"
                }
                xDataFormatted.add(xValsDateLabelFormatted)
            }

            return if (value.toInt() >= 0 && value.toInt() <= xDataFormatted.size - 1) {
                xDataFormatted[value.toInt()]
            } else {
                ("").toString()
            }
        }
    }
    fun updatePlot(airHumEntries:ArrayList<Entry>,batChargeEntries:ArrayList<Entry>,lumEntries:ArrayList<Entry>,soilHumEntries:ArrayList<Entry>,tempEntries:ArrayList<Entry>,
                    timeArr:ArrayList<String>, timeFmType:Int){
        val airHumSet = LineDataSet(airHumEntries,"Air Humidity")
        airHumSet.color = Color.BLACK
        val batChargeSet = LineDataSet(batChargeEntries, "Battery Charge")
        batChargeSet.color = Color.YELLOW
        val lumSet = LineDataSet(lumEntries,"Luminosity")
        lumSet.color = Color.DKGRAY
        val soilHumSet = LineDataSet(soilHumEntries,"Soil Humidity")
        soilHumSet.color = Color.BLUE
        val tempSet = LineDataSet(tempEntries, "Temperature")
        tempSet.color = Color.RED

        val chartdata = ArrayList<ILineDataSet>()
        chartdata.add(airHumSet)
        chartdata.add(batChargeSet)
        chartdata.add(lumSet)
        chartdata.add(soilHumSet)
        chartdata.add(tempSet)

        lchart.description.text = ""



        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = MyValueFormatter(timeFmType, timeArr)
        xAxis.setLabelCount(5,false)
        val yAxisRight: YAxis = lchart.axisRight
        yAxisRight.setDrawLabels(false)


        val lineData = LineData(chartdata)
        lchart.data = lineData
        lchart.invalidate() // refresh
    }
}