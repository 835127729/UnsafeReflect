package com.muye.unsafereflect

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.muye.unsafereflect.ui.theme.UnsafeReflectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            content()
        }
    }

    private val viewModel: TestUnsafeReflectViewModel = TestUnsafeReflectViewModel()

    @Composable
    @Preview(showSystemUi = true)
    fun content() {
        UnsafeReflectTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
                ) {
                    Card() {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Button(onClick = {
                                if (viewModel.testReflectMethod(this@MainActivity)) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unsafe反射方法测试成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unsafe反射方法测试失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Text("Unsafe反射方法测试")
                            }
                            Button(onClick = {
                                // 读取成员变量
                                // 写入成员变量
                                if (viewModel.testReflectField(this@MainActivity)) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unsafe反射属性测试成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unsafe反射属性测试失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Text("Unsafe反射属性测试")
                            }
                        }
                    }

                    Card(modifier = Modifier.offset(x = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Button(onClick = {
                                // 调用隐藏API
                                if (viewModel.testHiddenApi()) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Invoke Hidden API Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Invoke Hidden API Fail",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, colors = ButtonDefaults.filledTonalButtonColors()) {
                                Text("Invoke Hidden API")
                            }
                            Button(onClick = {
                                // Bypass
                                if (viewModel.bypassTestApi()) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Bypass Hidden API Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Bypass Hidden API Fail",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                Text("Bypass Hidden API")
                            }
                        }
                    }

                    Card {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Triggering this bug on Android 7.1 or below",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                viewModel.invokeAndroidBug(this@MainActivity)
                            }, colors = ButtonDefaults.filledTonalButtonColors()) {
                                Text("Android Pdf Bug Invoke In Android 7.1")
                            }
                            Button(onClick = {
                                viewModel.fixAndroidBug(this@MainActivity)
                            }) {
                                Text("Android Pdf Bug Fix In Android 7.1")
                            }
                        }
                    }
                }
            }
        }
    }
}