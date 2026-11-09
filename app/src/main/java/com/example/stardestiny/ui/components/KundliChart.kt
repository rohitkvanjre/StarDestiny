package com.example.stardestiny.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A South Indian Style Kundli Chart.
 * 4x4 Perimeter grid with 12 cells.
 * Houses flow CLOCKWISE starting from the Lagna (Ascendant).
 */
@Composable
fun KundliChart(
    ascendantIndex: Int, // Rashi Index (0-11, 0=Aries/Mesha)
    planetPositions: Map<String, Int>, // Planet Name -> Rashi Index (0-11)
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    // South Indian Rashi Mapping (Fixed positions in the grid)
    // 11 (Pisces) is top-left (0,0). Aries (0) is (0,1), and so on clockwise.
    val rashiToCoord = mapOf(
        11 to Pair(0, 0), 0 to Pair(0, 1), 1 to Pair(0, 2), 2 to Pair(0, 3),
        3 to Pair(1, 3), 4 to Pair(2, 3), 5 to Pair(3, 3), 6 to Pair(3, 2),
        7 to Pair(3, 1), 8 to Pair(3, 0), 9 to Pair(2, 0), 10 to Pair(1, 0)
    )

    // Map to easily find Rashi for a given grid coordinate
    val coordToRashi = rashiToCoord.entries.associateBy({ it.value }, { it.key })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface)
    ) {
        for (row in 0..3) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..3) {
                    val rashiIdx = coordToRashi[Pair(row, col)]
                    
                    if (rashiIdx != null) {
                        // Perimeter Cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, borderColor)
                        ) {
                            ChartCell(
                                rashiIdx = rashiIdx,
                                ascendantIndex = ascendantIndex,
                                planets = planetPositions.filter { it.value == rashiIdx }.keys.toList()
                            )
                        }
                    } else {
                        // Central 2x2 empty area
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCell(
    rashiIdx: Int,
    ascendantIndex: Int,
    planets: List<String>
) {
    // Clockwise Rashi sequence for South Indian chart
    val rashisInOrder = listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    
    // Calculate house number: Lagna is House 1, then clockwise
    val currentPos = rashisInOrder.indexOf(rashiIdx)
    val lagnaPos = rashisInOrder.indexOf(ascendantIndex)
    val houseNum = ((currentPos - lagnaPos + 12) % 12) + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        // House Number indicator (bottom-left)
        Text(
            text = houseNum.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // Content: Lagna and Planets
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (rashiIdx == ascendantIndex) {
                Text(
                    text = "Lagna",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            // Stack planets vertically with adaptive wrapping
            planets.forEach { planet ->
                Text(
                    text = planet,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
