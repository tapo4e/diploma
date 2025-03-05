package com.example.diploma.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ScanningScreen(modifier: Modifier = Modifier,
                   goToMainScreen:() -> Unit,
                   state:StateFlow<Boolean>) {
    val stay by state.collectAsState()
    val infiniteScale = rememberInfiniteTransition(label = "")
    val scale by infiniteScale.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    LaunchedEffect(stay) {
        if(stay)
        goToMainScreen()
    }
    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF6495ED)))
    {

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(80.dp)

        ){
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = 0xFF739fee.toInt()
                setShadowLayer(
                    12.dp.toPx(),
                    0f,
                    2.dp.toPx(),
                    0x58585840
                )
            }
            drawCircle(
                color = Color(0xFF739fee).copy(alpha = 0.5f),
                radius = 140.dp.toPx()*scale

            )
            drawCircle(
                color = Color(0xFF739fee).copy(alpha = 0.6f),
                radius = 110.dp.toPx()*scale

            )
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawCircle(
                    center.x, center.y, 80.dp.toPx(), paint
                )
            }

        }
        Text(text = "Подключение",
            modifier.align(Alignment.Center),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xffe5e5e5))
    }
}

@Preview
@Composable
fun ScanningScreenPreview(){
    ScanningScreen(goToMainScreen = {}, state = MutableStateFlow(true))
}