#### [(${title})]
[# th:if="${total > 0}"]
[(${icon})] **[(${result})]** : [(${covered})] / [(${total})] ([(${#numbers.formatPercent(1.0 * covered / total, 2, 2)})])
[/]
[# th:if="${total <= 0}"]
[(${icon})] **[(${result})]** : [(${covered})] / [(${total})] (0%)
[/]

[# th:if="${detail != null && detail.size > 0}"]
#### file detail

|   |path|covered line|new line|coverage|
|----|----|----|----|----|
[# th:each="file : ${detail}"]|[(${file.icon})]|<details close><summary>[[(${file.name})]]([(${file.url})])</summary><ul>[# th:each="line : ${file.addedLine}"]<li>[# th:if="${line.value.name() == 'COVERED'}"]:green_heart:[/][# th:if="${line.value.name() == 'UNCOVERED'}"]:heart:[/][# th:if="${line.value.name() == 'CONDITION'}"]:yellow_heart:[/][[(${'[Line ' + line.key.start}) + (${line.key.start==line.key.end} ? ']' : '-' + ${line.key.end} + ']')]]([(${file.url}+'#L'+${line.key.start})])</li>[/]</ul></details>|[(${file.addedCoverLine})]|[(${file.addedLineCount})]|[(${#numbers.formatPercent(1.0 * file.addedCoverLine / file.addedLineCount, 2, 2)})]|
[/]

[/]