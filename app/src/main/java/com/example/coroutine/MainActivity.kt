package com.example.coroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val JOB_TIMEOUT = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get data
        button.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest1()
            }
        }

        //get data sequentially
        button1.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest2()
            }
        }

        //get data parallel
        button2.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest3()
            }
        }

        //get data with time out
        button3.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest4()
            }
        }
    }

    //get data
    private suspend fun apiRequest1() {

        val result1 = getResult1FromApi() // wait until job is done
        setTextOnMainThread("Got $result1")

        val result2 = getResult2FromApi() // wait until job is done
        setTextOnMainThread("Got $result2")
    }

    //get data sequentially
    private suspend fun apiRequest2() {
        withContext(IO) {
            val executionTime = measureTimeMillis {

                //method 1
                val job1 = launch{
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                job1.join()

                //method 2
                val result2 = async {
                    getResult2FromApi()
                }.await()
                setTextOnMainThread("Got $result2")
            }
            setTextOnMainThread("Total Time ${executionTime} ms")
        }
    }

    //get data parallel
    private suspend fun apiRequest3() {
        withContext(IO) {

            val job1 = launch {
                val time1 = measureTimeMillis {
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                println("debug: compeleted job1 in $time1 ms.")
            }

            val job2 = launch {
                val time2 = measureTimeMillis {
                    val result2 = getResult2FromApi()
                    setTextOnMainThread("Got $result2")
                }
                setTextOnMainThread("Total Time $time2 ms.")
            }

        }
    }

    //get data with time out
    private suspend fun apiRequest4() {
        withContext(IO) {

            val job = withTimeoutOrNull(JOB_TIMEOUT) {

                val result1 = getResult1FromApi() // wait until job is done
                setTextOnMainThread("Got $result1")

                val result2 = getResult2FromApi() // wait until job is done
                setTextOnMainThread("Got $result2")

            } // waiting for job to complete...

            if(job == null){
                setTextOnMainThread("Cancelling job... took longer than $JOB_TIMEOUT ms")
            }

        }
    }

    private suspend fun getResult1FromApi(): String {
        // Does not block thread. Just suspends the coroutine inside the thread
        delay(1000)
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        // Does not block thread. Just suspends the coroutine inside the thread
        delay(1000)
        return "Result #2"
    }

    //append text to the textview
    private fun setNewText(input: String){
        val newText = textView.text.toString() + "\n$input"
        textView.text = newText
    }

    //change context to main and call setNewText
    private suspend fun setTextOnMainThread(input: String) {
        withContext (Main) {
            setNewText(input)
        }
    }
}
