import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import flowviz.resources.Res
import flowviz.resources.pretendard_bold
import flowviz.resources.pretendard_regular
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font

// ============================================================
// Cold Flow 씬
// - collect 하기 전엔 flow 블록이 실행조차 안 됨
// - 구독자(A/B)마다 "처음부터 새로" 실행됨  → 실제 flow{}를 돌려서 시각화
// ============================================================

private val Bg = Color(0xFF0F1117)
private val Card = Color(0xFF181B24)
private val AccentA = Color(0xFF4F9DFF)
private val AccentB = Color(0xFFFFB84F)
private val Muted = Color(0xFF8A90A2)

/** 실제 Cold Flow. collect 될 때마다 블록이 처음부터 다시 실행된다. */
private fun coldNumberFlow(onBlockRun: () -> Unit) = flow {
    onBlockRun()                       // ← collect 시작 시점에만 실행됨
    for (i in 1..4) {
        delay(650)
        emit(i)
    }
}

@Composable
fun App() {
    val appFont = FontFamily(
        Font(Res.font.pretendard_regular, FontWeight.Normal),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
    )

    MaterialTheme(colorScheme = darkColorScheme(background = Bg)) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = appFont)
        ) {
            ColdFlowScene()
        }
    }
}

@Composable
private fun ColdFlowScene() {
    var laneA by remember { mutableStateOf(listOf<Int>()) }
    var laneB by remember { mutableStateOf(listOf<Int>()) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    fun reset() {
        laneA = emptyList()
        laneB = emptyList()
        logs = emptyList()
    }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 28.dp)
    ) {
        Text("Flow Viz", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Cold Flow — collect 해야 흐른다",
            color = Muted, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(24.dp))
 
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

            OutlinedButton(onClick = { reset() }) { Text("Reset", color = Muted) }
        }

        Spacer(Modifier.height(28.dp))

        Column(
            Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val sourceMarbles = if (laneA.isEmpty() && laneB.isEmpty()) emptyList() else (1..4).toList()
            MarbleLane("Source", "flow { }", sourceMarbles, Muted, dimmed = true)
            MarbleLane("수집자 A", "collect", laneA, AccentA)
            MarbleLane("수집자 B", "collect", laneB, AccentB)
        }

        Spacer(Modifier.height(24.dp))

        Column(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(20.dp)) {
            Text("실행 로그", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (logs.isEmpty()) {
                Text(
                    "아직 아무도 collect 안 함 → flow 블록은 실행조차 안 됐음",
                    color = Muted, fontSize = 14.sp
                )
            } else {
                logs.forEach {
                    Text(it, color = Color(0xFFCED3E0), fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "A와 B를 각각 눌러보세요. 구독자마다 flow 블록이 \"처음부터 새로\" 실행돼 " +
                "1,2,3,4를 독립적으로 받습니다. 이게 Cold Flow의 핵심.",
            color = Muted, fontSize = 13.sp
        )
    }
}

@Composable
private fun MarbleLane(label: String, sub: String, marbles: List<Int>, color: Color, dimmed: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(96.dp)) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = Muted, fontSize = 11.sp)
        }
        Box(
            Modifier.height(52.dp).fillMaxWidth()
                .background(Color(0xFF11141C), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                marbles.forEachIndexed { index, n ->
                    Marble(n, if (dimmed) color.copy(alpha = 0.35f) else color, key = "$label-$index")
                }
            }
        }
    }
}

@Composable
private fun Marble(n: Int, color: Color, key: String) {
    // key 가 바뀌면 새 컴포저블 → LaunchedEffect 로 pop-in 애니메이션
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 260f)
    )
    Box(
        Modifier.size(36.dp).scale(scale).background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(n.toString(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
