import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * map / combine 씬.
 *
 * map은 흐르는 각 값을 변환하고(1 flow → 1 flow), combine은 여러 flow를
 * 합쳐 파생값을 만든다(어느 입력이 바뀌어도 블록이 다시 실행됨).
 * 이 앱의 filteredSubscriptions = combine(구독목록, 선택필터) 패턴과 같다.
 */
@Composable
fun MapCombineScene() {
    // ---------- map ----------
    val numbers = remember { MutableStateFlow(1) }
    var srcLane by remember { mutableStateOf(listOf(1)) }
    var mapLane by remember { mutableStateOf(listOf<Int>()) }
    LaunchedEffect(Unit) {
        numbers.map { it * 2 }.collect { mapLane = mapLane + it }
    }

    // ---------- combine ----------
    val itemsFlow = remember { MutableStateFlow(listOf("A(active)", "B(canceled)", "C(active)")) }
    val filterFlow = remember { MutableStateFlow("ALL") }
    var result by remember { mutableStateOf(listOf<String>()) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    LaunchedEffect(Unit) {
        combine(itemsFlow, filterFlow) { items, f ->
            if (f == "ACTIVE") items.filter { it.contains("active") } else items
        }.collect {
            result = it
            logs = logs + "combine 재계산 → ${it.size}개"
        }
    }

    SceneScaffold(Scene.MapCombine.title, Scene.MapCombine.subtitle) {
        // ===== map =====
        Text("map — 각 값을 변환 (1 flow → 1 flow)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { numbers.value = 1; srcLane = srcLane + 1 },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("값 = 1", color = Color.White, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { numbers.value = 5; srcLane = srcLane + 5 },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("값 = 5", color = Color.White, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(14.dp))
        DiagramCard {
            MarbleLane("Source", "numbers", srcLane, Muted, dimmed = true)
            MarbleLane("Result", "map { *2 }", mapLane, AccentA)
        }

        Spacer(Modifier.height(32.dp))

        // ===== combine =====
        Text("combine — 여러 flow를 합쳐 파생값 (어느 쪽이 바뀌어도 재계산)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { filterFlow.value = "ALL" },
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("필터 = ALL", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }
            Button(
                onClick = { filterFlow.value = "ACTIVE" },
                colors = ButtonDefaults.buttonColors(containerColor = AccentB)
            ) { Text("필터 = ACTIVE", color = Color(0xFF1A1200), fontWeight = FontWeight.Bold) }
            Button(
                onClick = { itemsFlow.value = itemsFlow.value + "D(active)" },
                colors = ButtonDefaults.buttonColors(containerColor = AccentA)
            ) { Text("목록에 D 추가", color = Color.White, fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = {
                itemsFlow.value = listOf("A(active)", "B(canceled)", "C(active)")
                filterFlow.value = "ALL"
                logs = emptyList()
            }) { Text("Reset", color = Muted) }
        }
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InputRow("list", itemsFlow.collectAsStateList(), AccentA)
            InputRow("filter", listOf(filterFlow.value), AccentB)
            Box(Modifier.fillMaxWidth().height(1.dp).background(LaneBg))
            InputRow("combine 결과", result, Color.White)
        }

        Spacer(Modifier.height(24.dp))
        LogPanel(logs, "필터나 목록을 바꿔보세요. 둘 중 뭐가 바뀌든 combine 블록이 다시 돕니다.")
        Hint(
            "filter만 바꿔도, list만 바꿔도 combine 블록이 재실행돼 결과가 갱신됩니다. " +
                "이 앱의 filteredSubscriptions = combine(구독목록, 선택필터)와 똑같은 패턴이에요."
        )
    }
}

/** 라벨 + 값 칩들 한 줄 */
@Composable
private fun InputRow(label: String, values: List<String>, color: Color) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { Chip(it, color) }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        Modifier.background(color.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** MutableStateFlow<List> 현재값을 그대로 읽는 헬퍼 (combine 입력 표시용) */
@Composable
private fun MutableStateFlow<List<String>>.collectAsStateList(): List<String> = value
