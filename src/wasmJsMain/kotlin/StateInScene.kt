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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * stateIn 씬 — Cold flow를 Hot StateFlow로 변환.
 *
 * upstream을 딱 1번만 돌려 여러 구독자가 공유하고 최신값을 보관한다.
 * 그래서 늦게 합류한 B도 0부터가 아니라 공유된 "현재값"부터 받는다
 * (stateIn 없이 cold였다면 B도 0부터 새로 시작했을 것).
 */
@Composable
fun StateInScene() {
    var epoch by remember { mutableStateOf(0) }
    key(epoch) {
        StateInContent(onReset = { epoch++ })
    }
}

/** Reset 시 stateIn/collector를 새로 만들기 위해 key 로 감싸는 실제 본문. */
@Composable
private fun StateInContent(onReset: () -> Unit) {
    val scope = rememberCoroutineScope()

    // 500ms마다 1씩 증가하는 cold flow (구독해야 실행됨)
    val cold = remember {
        flow {
            var i = 0
            while (i <= 12) {
                emit(i++)
                delay(500)
            }
        }
    }

    // stateIn 으로 cold → hot 변환: upstream 1번만 돌고 여러 구독자가 공유 + 최신값 보관
    val hot: StateFlow<Int> = remember {
        cold.stateIn(scope, SharingStarted.WhileSubscribed(5000), initialValue = -1)
    }

    var laneA by remember { mutableStateOf(listOf<Int>()) }
    var laneB by remember { mutableStateOf(listOf<Int>()) }
    var aOn by remember { mutableStateOf(false) }
    var bOn by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }

    SceneScaffold(Scene.StateIn.title, Scene.StateIn.subtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    aOn = true
                    logs = logs + "A 구독 시작 → upstream 실행됨 (0부터)"
                    scope.launch { hot.collect { laneA = (laneA + it).takeLast(10) } }
                },
                enabled = !aOn,
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("구독자 A 시작", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    bOn = true
                    logs = logs + "B 구독 시작 → 최신값 ${hot.value}부터 (0 아님! upstream 공유)"
                    scope.launch { hot.collect { laneB = (laneB + it).takeLast(10) } }
                },
                enabled = aOn && !bOn,
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("구독자 B 시작 (늦게)", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }

            OutlinedButton(onClick = onReset) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(28.dp))

        DiagramCard {
            MarbleLane("수집자 A", if (aOn) "구독 중" else "미구독", laneA, AccentA)
            MarbleLane("수집자 B", if (bOn) "늦게 합류" else "미구독", laneB, AccentB)
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(
            logs,
            "구독 전에도 hot.value = -1 (초기값)로 즉시 읽혀요. A를 먼저 시작해 값이 올라간 뒤 B를 눌러보세요."
        )

        Hint(
            "stateIn은 cold flow를 hot StateFlow로 바꿔요. upstream을 딱 1번만 돌리고 여러 구독자가 공유하죠. " +
                "그래서 B는 0부터가 아니라 A와 같은 \"현재값\"부터 받습니다. " +
                "만약 stateIn 없이 cold였다면 B도 0부터 새로 시작했을 거예요 (Cold Flow 씬과 비교!)."
        )
    }
}
