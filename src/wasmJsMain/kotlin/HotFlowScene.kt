import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Hot Flow(StateFlow) 씬.
 *
 * StateFlow의 두 성질을 보여준다.
 * 1) conflation — 이전과 같은 값을 넣으면 emit 되지 않는다.
 * 2) 상태 보관 + 공유 — 늦게 합류한 구독자도 최신값을 즉시 받는다.
 */
@Composable
fun HotFlowScene() {
    // Reset 시 collector 코루틴까지 깨끗이 정리하려고 key 로 전체를 새로 만든다
    var epoch by remember { mutableStateOf(0) }
    key(epoch) {
        HotFlowContent(onReset = { epoch++ })
    }
}

/** Reset 시 collector 코루틴까지 새로 만들기 위해 key 로 감싸는 실제 본문. */
@Composable
private fun HotFlowContent(onReset: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state = remember { MutableStateFlow(0) } // 초기값 0을 항상 "보관"

    var sourceLane by remember { mutableStateOf(listOf(0)) }
    var laneA by remember { mutableStateOf(listOf<Int>()) }
    var laneB by remember { mutableStateOf(listOf<Int>()) }
    var bJoined by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }

    // 수집자 A: 씬에 들어오면 바로 구독 (hot 이라 값 흐름을 공유)
    LaunchedEffect(Unit) {
        state.collect { laneA = laneA + it }
    }

    fun setValue(v: Int) {
        if (state.value == v) {
            logs = logs + "값 $v 다시 넣음 → 이전과 같아서 emit 안 됨 (conflation)"
        } else {
            state.value = v
            sourceLane = sourceLane + v
            logs = logs + "값 = $v → emit"
        }
    }

    SceneScaffold(Scene.Hot.title, Scene.Hot.subtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { setValue(1) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("값 = 1", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { setValue(2) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("값 = 2", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { setValue(2) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBug)
            ) { Text("같은 값 2 또 넣기", color = Color.White, fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    bJoined = true
                    logs = logs + "B 합류 → 구독하자마자 최신값 ${state.value} 즉시 받음"
                    scope.launch { state.collect { laneB = laneB + it } }
                },
                enabled = !bJoined,
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("늦은 구독자 B 합류", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }

            OutlinedButton(onClick = onReset) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(28.dp))

        DiagramCard {
            MarbleLane("Source", "state.value", sourceLane, Muted, dimmed = true)
            MarbleLane("수집자 A", "처음부터 구독", laneA, AccentA)
            MarbleLane(
                "수집자 B",
                if (bJoined) "늦게 합류" else "아직 미구독",
                laneB,
                AccentB
            )
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(logs, "값을 바꿔보세요. A는 이미 구독 중이라 초기값 0부터 받고 있어요.")

        Hint(
            "1) 같은 값 2를 또 넣으면 로그만 남고 마블은 안 늘어요 → conflation(중복 무시). " +
                "2) 값을 몇 번 바꾼 뒤 B를 합류시키면, B의 첫 마블이 1이 아니라 \"현재 최신값\"이에요 " +
                "→ Cold와 정반대로, 늦게 와도 최신 상태를 즉시 받습니다."
        )
    }
}
