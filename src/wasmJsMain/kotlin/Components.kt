import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 씬 공통 골격: 제목 + 부제 + 스크롤되는 본문 */
@Composable
fun SceneScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp, vertical = 32.dp)
    ) {
        Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(24.dp))
        content()
    }
}

/** 마블 다이어그램 한 줄 (라벨 + 흐르는 구슬들) */
@Composable
fun MarbleLane(label: String, sub: String, marbles: List<Int>, color: Color, dimmed: Boolean = false) {
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

/** 실행 로그 패널 */
@Composable
fun LogPanel(logs: List<String>, emptyText: String) {
    Column(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(20.dp)) {
        Text("실행 로그", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (logs.isEmpty()) {
            Text(emptyText, color = Muted, fontSize = 14.sp)
        } else {
            logs.forEach { Text(it, color = TextMain, fontSize = 14.sp) }
        }
    }
}

/** 씬 하단 힌트 문구 */
@Composable
fun Hint(text: String) {
    Spacer(Modifier.height(20.dp))
    Text(text, color = Muted, fontSize = 13.sp)
}

/** 마블 다이어그램들을 감싸는 카드 */
@Composable
fun DiagramCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content
    )
}
