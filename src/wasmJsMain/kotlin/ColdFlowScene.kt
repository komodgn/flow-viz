import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/** collect 될 때마다 블록이 처음부터 다시 실행되는 진짜 Cold Flow */
private fun coldNumberFlow(onBlockRun: () -> Unit) = flow {
    onBlockRun()                       // <- collect 시작 시점에만 실행됨
    for (i in 1..4) {
        delay(650)
        emit(i)
    }
}

/**
 * Cold Flow 씬.
 *
 * collect 하기 전엔 flow 블록이 실행조차 안 되고, 구독자(A/B)마다 블록이
 * "처음부터 새로" 실행된다는 걸 실제 [coldNumberFlow]를 돌려 시각화한다.
 */
@Composable
fun ColdFlowScene() {
    var laneA by remember { mutableStateOf(listOf<Int>()) }
    var laneB by remember { mutableStateOf(listOf<Int>()) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    SceneScaffold(Scene.Cold.title, Scene.Cold.subtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    laneA = emptyList()
                    scope.launch {
                        coldNumberFlow { logs = logs + "A가 collect → flow 블록 실행됨" }
                            .collect { laneA = laneA + it }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("Collect A", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    laneB = emptyList()
                    scope.launch {
                        coldNumberFlow { logs = logs + "B가 collect → flow 블록 또 실행됨 (처음부터!)" }
                            .collect { laneB = laneB + it }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("Collect B", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }

            OutlinedButton(onClick = {
                laneA = emptyList(); laneB = emptyList(); logs = emptyList()
            }) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(28.dp))

        DiagramCard {
            val source = if (laneA.isEmpty() && laneB.isEmpty()) emptyList() else (1..4).toList()
            MarbleLane("Source", "flow { }", source, Muted, dimmed = true)
            MarbleLane("수집자 A", "collect", laneA, AccentA)
            MarbleLane("수집자 B", "collect", laneB, AccentB)
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(logs, "아직 아무도 collect 안 함 → flow 블록은 실행조차 안 됐음")

        Hint(
            "A와 B를 각각 눌러보세요. 구독자마다 flow 블록이 \"처음부터 새로\" 실행돼 " +
                "1,2,3,4를 독립적으로 받습니다. 이게 Cold Flow의 핵심."
        )
    }
}
