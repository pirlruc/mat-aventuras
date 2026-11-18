package pt.mataventuras.domain.parent

/**
 * One child on the teacher dashboard. [cells] is that child's mastery grid.
 */
data class ClassroomChild(
    val name: String,
    val cells: List<MasteryCell>,
)

/**
 * CSV of every child's mastery cells. A child with no cells still gets a row.
 */
fun classroomCsv(children: List<ClassroomChild>): String {
    val header = "name,module,band,hits,misses"
    val rows =
        children.flatMap { child ->
            if (child.cells.isEmpty()) {
                listOf("${csvField(child.name)},,,0,0")
            } else {
                child.cells.map { cell ->
                    listOf(
                        csvField(child.name),
                        cell.module.name,
                        cell.band.name,
                        cell.hits.toString(),
                        cell.misses.toString(),
                    ).joinToString(",")
                }
            }
        }
    return (listOf(header) + rows).joinToString("\n")
}

private fun csvField(value: String): String {
    val quoted = value.any { it == ',' || it == '"' || it == '\n' }
    if (!quoted) return value
    return "\"${value.replace("\"", "\"\"")}\""
}
