import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The Bug 씬 — StateFlow conflation 때문에 생긴 hang 재현.
 *
 * 같은 값(emptyList)으로 갱신하면 emit이 생기지 않는데, `drop(1).first()`로
 * "다음 emit"을 기다리면 그게 영영 오지 않아 멈춘다(버그 버전).
 * 고친 버전은 emit을 기다리지 않고 값을 직접 받아 즉시 반환한다.
 */
@Composable
fun BugScene() {
    val scope = rememberCoroutineScope()
    val subscriptions = remember { MutableStateFlow<List<String>>(emptyList()) }

    var status by remember { mutableStateOf("대기 중") }
    var statusColor by remember { mutableStateOf(Muted) }
    var running by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }

    SceneScaffold(Scene.Bug.title, Scene.Bug.subtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (running) return@Button
                    scope.launch {
                        running = true
                        status = "대기 중... (다음 emit 기다리는 중)"
                        statusColor = AccentBug
                        logs = listOf(
                            "subscriptions.value = emptyList()  → 기존과 같은 값이라 emit 없음",
                            "subscriptions.drop(1).first()  → '다음 emit'을 기다리기 시작...",
                        )
                        val result = withTimeoutOrNull(3000) {
                            subscriptions.value = emptyList()   // 같은 값 -> emit 안 생김
                            subscriptions.drop(1).first()       // 다음 emit 대기 -> 영영 안 옴
                        }
                        running = false
                        if (result == null) {
                            status = "3초 타임아웃 — 영영 안 옴 (hang 재현됨)"
                            statusColor = AccentBug
                            logs = logs + "결과: null  → 3초 내내 멈춰 있었다는 뜻"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBug)
            ) { Text("새로고침 — 버그 버전", color = Color.White, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    val latest = emptyList<String>()  // 실제로는 repository.getSubscriptions()
                    subscriptions.value = latest
                    status = "즉시 반환됨 (결과: ${latest.firstOrNull()})"
                    statusColor = AccentOk
                    logs = listOf(
                        "값이 같든 다르든 emit을 기다리지 않고 직접 반환",
                        "결과: ${latest.firstOrNull()}  → 절대 멈추지 않음",
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentOk)
            ) { Text("새로고침 — 고친 버전", color = Color(0xFF06210F), fontWeight = FontWeight.Bold) }

            OutlinedButton(onClick = {
                status = "대기 중"; statusColor = Muted; logs = emptyList()
            }) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(24.dp))

        // 상태 카드
        Row(
            Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (running) {
                CircularProgressIndicator(Modifier.size(28.dp), color = AccentBug, strokeWidth = 3.dp)
            } else {
                Box(Modifier.size(28.dp)) // 자리 유지
            }
            Column {
                Text("refreshAndAwaitActive() 상태", color = Muted, fontSize = 12.sp)
                Text(status, color = statusColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(logs, "버그 버전을 눌러 hang을 재현하거나, 고친 버전과 비교해 보세요.")

        Hint(
            "StateFlow는 이전과 같은 값이면 emit을 안 해요. 그런데 버그 코드는 drop(1).first()로 " +
                "\"다음 emit\"을 기다렸기 때문에, 갱신 결과가 같으면(emptyList → emptyList) 그 emit이 " +
                "영영 오지 않아 hang이 났죠. 고친 버전은 emit을 기다리지 않고 값을 직접 받아 즉시 반환합니다."
        )
    }
}
