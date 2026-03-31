package com.example.bmiapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    // Simpan status satuan (metrik atau imperial)
    private var isMetric = true

    // Biar gak error kalau slider ama kotak input saling update barengan
    private var isSyncing = false

    // Deklarasi semua komponen UI yang dipake
    private lateinit var radioGroupUnit : RadioGroup
    private lateinit var radioMetric    : RadioButton
    private lateinit var radioImperial  : RadioButton

    private lateinit var etHeight       : EditText
    private lateinit var etWeight       : EditText
    private lateinit var etAge          : EditText

    private lateinit var seekBarHeight  : SeekBar
    private lateinit var seekBarWeight  : SeekBar
    private lateinit var seekBarAge     : SeekBar

    private lateinit var tvHeightUnit   : TextView
    private lateinit var tvWeightUnit   : TextView
    private lateinit var tvHeightMin    : TextView
    private lateinit var tvHeightMax    : TextView
    private lateinit var tvWeightMin    : TextView
    private lateinit var tvWeightMax    : TextView

    private lateinit var btnCalculate   : Button
    private lateinit var cardResult     : CardView
    private lateinit var tvBmiScore     : TextView
    private lateinit var tvBmiLabel     : TextView
    private lateinit var tvTip          : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi semua view dan setting interaksinya
        bindViews()
        setupUnitToggle()
        setupHeightSync()
        setupWeightSync()
        setupAgeSync()

        // Pas tombol diklik, langsung hitung BMI-nya
        btnCalculate.setOnClickListener { calculate() }
    }

    // Hubungin variabel di atas sama ID yang ada di layout XML
    private fun bindViews() {
        radioGroupUnit = findViewById(R.id.radioGroupUnit)
        radioMetric    = findViewById(R.id.radioMetric)
        radioImperial  = findViewById(R.id.radioImperial)

        etHeight       = findViewById(R.id.etHeight)
        etWeight       = findViewById(R.id.etWeight)
        etAge          = findViewById(R.id.etAge)

        seekBarHeight  = findViewById(R.id.seekBarHeight)
        seekBarWeight  = findViewById(R.id.seekBarWeight)
        seekBarAge     = findViewById(R.id.seekBarAge)

        tvHeightUnit   = findViewById(R.id.tvHeightUnit)
        tvWeightUnit   = findViewById(R.id.tvWeightUnit)
        tvHeightMin    = findViewById(R.id.tvHeightMin)
        tvHeightMax    = findViewById(R.id.tvHeightMax)
        tvWeightMin    = findViewById(R.id.tvWeightMin)
        tvWeightMax    = findViewById(R.id.tvWeightMax)

        btnCalculate   = findViewById(R.id.btnCalculate)
        cardResult     = findViewById(R.id.cardResult)
        tvBmiScore     = findViewById(R.id.tvBmiScore)
        tvBmiLabel     = findViewById(R.id.tvBmiLabel)
        tvTip          = findViewById(R.id.tvTip)
    }

    // Fungsi buat ganti-ganti satuan (Misal dari CM/KG ke Inci/Lbs)
    private fun setupUnitToggle() {
        radioGroupUnit.setOnCheckedChangeListener { _, checkedId ->
            isMetric = (checkedId == R.id.radioMetric)

            // Kunci sinkronisasi biar gak bentrok pas ganti satuan
            isSyncing = true

            if (isMetric) {
                tvHeightUnit.text = "cm"
                tvWeightUnit.text = "kg"
                tvHeightMin.text  = "100 cm"
                tvHeightMax.text  = "220 cm"
                tvWeightMin.text  = "30 kg"
                tvWeightMax.text  = "150 kg"

                seekBarHeight.max      = 120   // Rentang 100–220 cm
                seekBarHeight.progress = 70    // Default 170 cm
                seekBarWeight.max      = 120   // Rentang 30–150 kg
                seekBarWeight.progress = 35    // Default 65 kg

                etHeight.setText("170")
                etWeight.setText("65")
            } else {
                tvHeightUnit.text = "in"
                tvWeightUnit.text = "lbs"
                tvHeightMin.text  = "48 in"
                tvHeightMax.text  = "90 in"
                tvWeightMin.text  = "66 lbs"
                tvWeightMax.text  = "330 lbs"

                seekBarHeight.max      = 42    // Rentang 48–90 in
                seekBarHeight.progress = 19    // Default 67 in
                seekBarWeight.max      = 132   // Rentang 66–330 lbs
                seekBarWeight.progress = 39    // Default 144 lbs

                etHeight.setText("67")
                etWeight.setText("144")
            }

            isSyncing = false
            // Sembunyiin hasil kalau satuannya diganti (biar fresh lagi)
            cardResult.visibility = View.GONE
        }
    }

    // Biar angka di kotak input sama slider tinggi badan selalu kompak/sama
    private fun setupHeightSync() {

        // Kalau slider digeser, angka di kotak input ikut berubah
        seekBarHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncing) return
                isSyncing = true
                val value = if (isMetric) 100 + progress else 48 + progress
                etHeight.setText(value.toString())
                etHeight.setSelection(etHeight.text.length)
                isSyncing = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Kalau angka di kotak input diketik manual, slider ikut geser
        etHeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (isSyncing) return
                val v = s.toString().toFloatOrNull() ?: return
                isSyncing = true
                if (isMetric) {
                    val clamped = v.coerceIn(100f, 220f).roundToInt()
                    seekBarHeight.progress = clamped - 100
                } else {
                    val clamped = v.coerceIn(48f, 90f).roundToInt()
                    seekBarHeight.progress = clamped - 48
                }
                isSyncing = false
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    // Biar angka di kotak input sama slider berat badan selalu kompak
    private fun setupWeightSync() {

        // Slider berat digeser -> Kotak input update
        seekBarWeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncing) return
                isSyncing = true
                val value = if (isMetric) 30 + progress else 66 + progress * 2
                etWeight.setText(value.toString())
                etWeight.setSelection(etWeight.text.length)
                isSyncing = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Kotak input berat diisi -> Slider update
        etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (isSyncing) return
                val v = s.toString().toFloatOrNull() ?: return
                isSyncing = true
                if (isMetric) {
                    val clamped = v.coerceIn(30f, 150f).roundToInt()
                    seekBarWeight.progress = clamped - 30
                } else {
                    val clamped = v.coerceIn(66f, 330f).roundToInt()
                    seekBarWeight.progress = ((clamped - 66) / 2).coerceIn(0, 132)
                }
                isSyncing = false
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    // Biar angka di kotak input sama slider umur selalu kompak
    private fun setupAgeSync() {

        // Slider umur digeser -> Kotak input update
        seekBarAge.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncing) return
                isSyncing = true
                val age = 10 + progress
                etAge.setText(age.toString())
                etAge.setSelection(etAge.text.length)
                isSyncing = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Kotak input umur diisi -> Slider update
        etAge.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (isSyncing) return
                val v = s.toString().toIntOrNull() ?: return
                isSyncing = true
                val clamped = v.coerceIn(10, 80)
                seekBarAge.progress = clamped - 10
                isSyncing = false
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    // Ini logika utama buat ngitung skor BMI dan nentuin kategorinya
    private fun calculate() {
        val heightRaw = etHeight.text.toString().toFloatOrNull()
        val weightRaw = etWeight.text.toString().toFloatOrNull()

        // Validasi dulu, jangan sampe kosong atau nol angkanya
        if (heightRaw == null || weightRaw == null || heightRaw <= 0 || weightRaw <= 0) {
            Toast.makeText(this, "Masukkan tinggi dan berat yang valid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kalau satuannya Imperial, kita ubah dulu ke Metrik buat itungannya
        val heightCm = if (isMetric) heightRaw else heightRaw * 2.54f
        val weightKg = if (isMetric) weightRaw else weightRaw * 0.453592f

        // Rumus BMI: Berat (kg) / Tinggi^2 (m)
        val heightM = heightCm / 100f
        val bmi     = weightKg / (heightM * heightM)
        val bmiRounded = (bmi * 10).roundToInt() / 10f

        // Nentuin label, warna teks, sama tips berdasarkan skor BMI
        val (label, color, tip) = when {
            bmi < 18.5 -> Triple(
                "Underweight", "#56CCF2",
                "Kamu termasuk kurus. Coba tingkatkan asupan kalori dan protein ya!"
            )
            bmi < 25.0 -> Triple(
                "Normal", "#27AE60",
                "Berat badanmu ideal! Pertahankan pola makan sehat dan olahraga rutin."
            )
            bmi < 30.0 -> Triple(
                "Overweight", "#F39C12",
                "Sedikit di atas normal. Kurangi gula & perbanyak aktivitas fisik."
            )
            else -> Triple(
                "Obese", "#FF6B9D",
                "Disarankan konsultasi ke dokter untuk program diet yang tepat."
            )
        }

        // Tampilan hasil akhirnya
        tvBmiScore.text = bmiRounded.toString()
        tvBmiLabel.text = label
        tvTip.text      = tip
        tvBmiScore.setTextColor(android.graphics.Color.parseColor(color))

        // Munculin kartu hasil yang tadinya sembunyi
        cardResult.visibility = View.VISIBLE
    }
}
