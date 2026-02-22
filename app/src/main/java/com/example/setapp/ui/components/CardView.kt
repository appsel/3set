package com.example.setapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.setapp.domain.model.Card as SetCard
import androidx.compose.ui.geometry.Size

@Composable
fun CardView(
    card: SetCard,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val lightYellow = Color(0xFFFCFF99)

    val cardColor = when (card.color) {
        0 -> Color(0xFFFF0101)
        1 -> Color(0xFF008002) // Green
        else -> Color(0xFF800080) // Purple
    }

    Card(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1.6f) // Landscape card
            .then(
                if (isSelected) Modifier.shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = lightYellow,
                    ambientColor = lightYellow
                ) else Modifier
            )
            .border(
                width = if (isSelected) 4.dp else 1.dp,
                color = if (isSelected) lightYellow else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 2.dp)
    ) {
        // Center the shapes horizontally and vertically
        Row(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(card.number + 1) { index ->
                ShapePainter(
                    modifier = Modifier.size(width = 20.dp, height = 40.dp),
                    shapeType = card.shape,
                    shadingType = card.shading,
                    color = cardColor
                )
                // Add spacing between shapes
                if (index < card.number) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
fun ShapePainter(
    modifier: Modifier = Modifier,
    shapeType: Int,
    shadingType: Int,
    color: Color
) {
    Canvas(modifier = modifier) {
        val path = when (shapeType) {
            0 -> getDiamondPath(size)
            1 -> getSquigglePath(size)
            else -> getStadiumPath(size)
        }

        val strokeWidthPx = 1.5.dp.toPx()

        when (shadingType) {
            0 -> drawPath(path, color, style = Fill)
            1 -> { // Striped
                drawPath(path, color, style = Stroke(width = strokeWidthPx))
                clipPath(path) {
                    var y = 0f
                    val stripeGap = 3.dp.toPx()
                    while (y < size.height) { // Horizontal stripes for vertical shape
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += stripeGap
                    }
                }
            }
            else -> drawPath(path, color, style = Stroke(width = strokeWidthPx)) // Open
        }
    }
}

private fun getDiamondPath(size: Size): Path = Path().apply {
    val w = size.width
    val h = size.height

    moveTo(w * 0.4958f, 0f)
    lineTo(0f, h * 0.4996f)
    lineTo(w * 0.4979f, h)
    lineTo(w, h * 0.4977f)
    close()
}

private fun getStadiumPath(size: Size): Path = Path().apply {
    val w = size.width
    val h = size.height

    moveTo(w * 0.0000f, h * 0.2215f)
    cubicTo(w * 0.0000f, h * 0.1012f, w * 0.2127f, h * 0.0000f, w * 0.4659f, h * 0.0000f)
    lineTo(w * 0.5341f, h * 0.0000f)
    cubicTo(w * 0.7873f, h * 0.0000f, w * 1.0000f, h * 0.1012f, w * 1.0000f, h * 0.2215f)
    lineTo(w * 1.0000f, h * 0.7785f)
    cubicTo(w * 1.0000f, h * 0.8988f, w * 0.7873f, h * 1.0000f, w * 0.5341f, h * 1.0000f)
    lineTo(w * 0.4659f, h * 1.0000f)
    cubicTo(w * 0.2127f, h * 1.0000f, w * 0.0000f, h * 0.8988f, w * 0.0000f, h * 0.7785f)
    lineTo(w * 0.0000f, h * 0.2215f)
    close()
}

private fun getSquigglePath(size: Size): Path = Path().apply {
    val w = size.width
    val h = size.height

    moveTo(w * 0.3242f, h * 0.0043f)

    cubicTo(
        w * 0.5615f, h * -0.0135f,
        w * 0.9135f, h * 0.0218f,
        w * 0.9886f, h * 0.1599f
    )
    cubicTo(
        w * 1.0637f, h * 0.2980f,
        w * 0.7424f, h * 0.5877f,
        w * 0.7412f, h * 0.7021f
    )
    cubicTo(
        w * 0.7663f, h * 0.8530f,
        w * 1.2013f, h * 0.9351f,
        w * 0.8638f, h * 0.9824f
    )
    cubicTo(
        w * 0.5263f, h * 1.0298f,
        w * 0.1714f, h * 0.9775f,
        w * 0.0747f, h * 0.8713f
    )
    cubicTo(
        w * -0.0220f, h * 0.7652f,
        w * 0.3452f, h * 0.5531f,
        w * 0.3280f, h * 0.3730f
    )
    cubicTo(
        w * 0.3109f, h * 0.1929f,
        w * 0.2080f, h * 0.1869f,
        w * 0.0557f, h * 0.1606f
    )
    cubicTo(
        w * -0.0966f, h * 0.1343f,
        w * 0.0869f, h * 0.0221f,
        w * 0.3242f, h * 0.0043f
    )

    close()
}

@Preview(showBackground = true, name = "Game Board Sample")
@Composable
fun CardViewPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        CardView(card = SetCard(1, 0, 0, 0, 0), modifier = Modifier.width(150.dp))
        Spacer(Modifier.height(8.dp))
        CardView(card = SetCard(2, 1, 1, 1, 1), modifier = Modifier.width(150.dp))
        Spacer(Modifier.height(8.dp))
        CardView(card = SetCard(3, 2, 2, 2, 2), modifier = Modifier.width(150.dp), isSelected = true)
    }
}

@Preview(showBackground = true, name = "Squiggle Detail")
@Composable
fun SquiggleDetailPreview() {
    Box(modifier = Modifier.padding(20.dp)) {
        // Large detail of 2 Purple Striped Squiggles
        CardView(
            card = SetCard(id = 100, shape = 1, color = 2, shading = 1, number = 1),
            modifier = Modifier.width(220.dp)
        )
    }
}
