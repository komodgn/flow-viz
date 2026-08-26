import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * SharedFlow 씬 — 일회성 이벤트에 SharedFlow가 맞는 이유.
 *
 * replay=0 이라 늦게 합류한 구독자는 지난 이벤트를 받지 못하고,
 * conflation이 없어 같은 값도 매번 정상 발행된다
 * (토스트·네비게이션 같은 일회성 이벤트에 적합).
 */
@Composable
fun SharedFlowScene() {
    var epoch by remember { mutableStateOf(0) }
    key(epoch) {
        SharedFlowContent(onReset = { epoch++ })
    }
}

/** Reset 시 collector 코루틴까지 새로 만들기 위해 key 로 감싸는 실제 본문. */
@Composable
private fun SharedFlowContent(onReset: () -> Unit) {
    val scope = rememberCoroutineScope()
    val events = remember { MutableSharedFlow<String>() } // replay=0 (기본)

    var laneA by remember { mutableStateOf(listOf<String>()) }
    var laneB by remember { mutableStateOf(listOf<String>()) }
    var bOn by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf<String>()) }

    // 수집자 A: 씬에 들어오면 바로 구독
    LaunchedEffect(Unit) {
        events.collect { laneA = laneA + it }
    }

    fun send(e: String) {
        scope.launch {
            events.emit(e)
            logs = logs + "emit(\"$e\")"
        }
    }

    SceneScaffold(Scene.Shared.title, Scene.Shared.subtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { send("토스트 표시") },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("토스트 표시", color = Color.White, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { send("화면 이동") },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("화면 이동", color = Color.White, fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    bOn = true
                    logs = logs + "B 합류 → 지금부터의 이벤트만 받음 (지난 건 안 옴)"
                    scope.launch { events.collect { laneB = laneB + it } }
                },
                enabled = !bOn,
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("늦은 구독자 B 합류", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }

            OutlinedButton(onClick = onReset) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(28.dp))

        Column(
            Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            EventLane("수집자 A", "처음부터 구독", laneA, AccentA)
            EventLane("수집자 B", if (bOn) "늦게 합류" else "아직 미구독", laneB, AccentB)
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(logs, "이벤트를 몇 개 보낸 뒤 B를 합류시키고, 다시 이벤트를 보내보세요.")

        Hint(
            "1) 같은 \"토스트 표시\"를 두 번 보내도 둘 다 수신돼요 → StateFlow의 conflation과 반대. " +
                "2) B를 나중에 합류시키면 합류 이전 이벤트는 못 받아요 (replay=0). " +
                "그래서 토스트·네비게이션 같은 일회성 이벤트엔 StateFlow가 아니라 SharedFlow가 맞습니다."
        )
    }
}

/** 문자열 이벤트를 칩으로 흐르게 보여주는 레인 */
@Composable
private fun EventLane(label: String, sub: String, events: List<String>, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(104.dp)) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = Muted, fontSize = 11.sp)
        }
        Box(
            Modifier.height(52.dp).fillMaxWidth().background(LaneBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                events.forEachIndexed { index, e ->
                    EventChip(e, color, key = "$label-$index")
                }
            }
        }
    }
}

@Composable
private fun EventChip(text: String, color: Color, key: String) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 260f)
    )
    Box(
        Modifier.scale(scale).background(color.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
