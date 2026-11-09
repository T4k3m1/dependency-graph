# Практическая работа №2, вариант 11

Инструмент на Java для визуализации зависимостей пакетов Python (pip).
Метаданные получает сам, через HTTP JSON API PyPI, без pip, Python
или сторонних библиотек получения зависимостей внутри приложения.
Граф строится BFS с рекурсией по уровням. Выход: D2 и SVG.
Пять этапов сохранены отдельными подробными Conventional/Scoped Commits.

## Требования к запуску

Нужен JDK 17+, команды java и javac в PATH. На Windows поддерживается JAVA_HOME.
Для этапа 5 нужен установленный официальный D2: https://d2lang.com/tour/install/.
На Windows можно установить MSI из официальных релизов D2, затем заново открыть терминал.
Проверка установки: `d2 version`. Проверенная версия рендерера - D2 0.9.0.
Для этапов 1-4 и основных тестов D2 не требуется.
Сетевой режим требует HTTP(S) доступа к репозиторию с совместимым PyPI JSON API.
Тестовый режим не обращается к внешней сети.

В Windows откройте папку проекта в Проводнике и введите cmd в адресной строке.
В этой командной строке выполняйте примеры ниже. В PowerShell добавляйте
префикс .\ перед run.bat и test.bat. На Linux/macOS замените run.bat на sh run.sh,
test.bat на sh test.sh.

## Сборка и тесты

```text
test.bat
test.bat --svg
```

Первый запуск проверяет CLI, ошибки, JSON, версии, маркеры, HTTP на localhost,
рекурсивный BFS, циклы, порядок установки, текст D2 и этапы CLI.
Второй дополнительно создаёт настоящие SVG: нужны D2 в PATH или D2_BIN.
Тесты не требуют JUnit, Python и доступа к внешнему интернету.
Локальный HTTP-сервер использует автоматически выбранный порт 127.0.0.1.
Assertions включены в скриптах через -ea.

Скрипты запуска компилируют src с --release 17 -encoding UTF-8 в build,
затем запускают depgraph.Main. Скрипты тестов компилируют src и tests.
Папка build игнорируется Git. Сборка Maven/Gradle и установка Java-зависимостей не нужны.

## Все настройки командной строки

| Опция | Значение и проверка |
| --- | --- |
| --package | Имя пакета PyPI; в тестовом режиме только A-Z |
| --repository | Базовый HTTP(S) URL, например https://pypi.org, либо путь к тестовому CSV |
| --test-mode | Строго true или false |
| --version | Точная стабильная числовая версия, например 2.32.3 |
| --output | Имя SVG-файла; расширение .svg, доступный каталог для записи |

Все пять опций обязательны. Порядок пар не важен, значения с пробелами берутся в кавычки.
Источником настроек служит только CLI. Неизвестные, повторные и пустые опции запрещены.
--stage 1..5 дополнительно выбирает операцию демонстрационного этапа; по умолчанию 5.
Относительные пути отсчитываются от рабочего каталога.
В тестовом режиме версия проверяется, но граф символический и версий вершин не содержит.

При ошибке выводится Error: ... в stderr и возвращается exit code 1; при успехе 0.
Проверяются все пять параметров, HTTP-ошибки, JSON, недоступные версии,
неразрешимые ограничения, несовместимая версия Python и ошибки D2.
Ошибки не подменяются пустыми графами. Каталоги выходного SVG создаются автоматически.

D2_BIN при необходимости задаёт путь к внешней установленной программе D2,
а не параметры анализа графа. Например, в PowerShell:

```powershell
$env:D2_BIN = "C:\Tools\d2.exe"
```

Замените пример на путь к вашему установленному D2. Если d2 доступен в PATH,
переменная не требуется. D2 вызывается только для рендеринга готового графа.

## Демонстрация пяти этапов

Этап 1 выводит только пять настроек ключ=значение:

```text
run.bat --package A --repository examples/repository.csv --test-mode true --version 1 --output out.svg --stage 1
```

Этап 2 выводит только прямые зависимости конкретного релиза:

```text
run.bat --package requests --repository https://pypi.org --test-mode false --version 2.32.3 --output out.svg --stage 2
```

Этап 3 строит транзитивный граф рекурсивным BFS и печатает список смежности:

```text
run.bat --package A --repository examples/repository.csv --test-mode true --version 1 --output out.svg --stage 3
```

Порядок BFS для ромба A-B/C-D: A, B, C, D. Рёбра циклов сохраняются,
обнаруженные вершины не добавляются на следующий уровень повторно.
Одна рекурсивная функция обрабатывает весь уровень, затем вызывает себя
для следующего. Это именно BFS с рекурсией, а не DFS.
Ограничение - 512 уровней; превышение даёт явную ошибку.

Этап 4 выводит только зависимость-первый порядок загрузки/установки:

```text
run.bat --package A --repository examples/repository.csv --test-mode true --version 1 --output out.svg --stage 4
```

Результат: D, B, C, A. Алгоритм Кана с алфавитным выбором готовых вершин.
Для E: D, B, C, A, E; для I: I. Для цикла G-H и самоссылки F полного
топологического порядка нет - выводится явная ошибка. На этапах 3 и 5 эти графы допустимы.
Порядок HTTP-загрузки метаданных BFS и порядок установки - разные последовательности.
[Сравнение с настоящим pip](docs/ORDER_COMPARISON.md) объясняет различия между независимыми пакетами.

Этап 5 по умолчанию формирует D2, печатает его и сохраняет SVG и одноимённый .d2:

```text
run.bat --package A --repository examples/repository.csv --test-mode true --version 1 --output generated/test.svg
```

Откройте generated/test.svg в браузере. Сгенерированные SVG/D2 не добавляются в Git.
Промежуточные отладочные выводы этапов 1, 2 и 4 не примешиваются к финальному выводу.

## Три настоящих пакета

```text
run.bat --package requests --repository https://pypi.org --test-mode false --version 2.32.3 --output out/requests.svg
run.bat --package rich --repository https://pypi.org --test-mode false --version 13.9.4 --output out/rich.svg
run.bat --package packaging --repository https://pypi.org --test-mode false --version 24.2 --output out/packaging.svg
```

Готовые SVG и D2 этих примеров передаются снаружи репозитория, в папке visualizations архива.
В docs/comparison сохранены текстовые результаты pip, наши графы, BFS и порядок установки.
[Сравнение](docs/COMPARISON.md): на трёх проверенных примерах совпали версии, вершины и рёбра.
Pip и pipdeptree использованы как внешние эталоны и не встроены в Java-приложение.

## Тестовый репозиторий

```csv
package,dependencies
A,B C
B,D
C,D
D,
```

Зависимости разделяются пробелами, лист имеет пустое поле. Все имена A-Z.
Все вершины должны быть объявлены; неизвестные и повторные вершины запрещены.
examples/repository.csv содержит также E -> A, F -> F, G -> H -> G и I без зависимостей.
Для демонстрации заменяйте значение --package на E, F, G или I.
Тесты проверяют также широкий граф из 4001 вершины и ограничение глубины.
Для демонстрации ошибок поочерёдно замените имя на lower в тестовом режиме,
путь на отсутствующий файл, --test-mode на yes, --version на latest, --output на out.png.

## Область поддержки

Метаданные получаются из /pypi/name/version/json для точной корневой версии.
Транзитивно выбираются новые совместимые стабильные числовые версии,
с учётом Requires-Python и yanked. Маркеры вычисляются для фиксированной
среды CPython 3.12.14 Windows AMD64, без extras. Установка Python не нужна.
Это учебный анализатор, не полный pip resolver. Полный PEP 440, extras,
прямые URL, ABI wheel, backtracking и некоторые маркеры не поддержаны.
Конфликты и неподдержанные применимые конструкции дают явную ошибку.
Подробности и возможные причины расхождений приведены в docs/COMPARISON.md.

## Оформление и передача

- [Соответствие заданию и перечню нарушений](docs/REQUIREMENTS.md).
- [Результаты проверок](docs/VERIFICATION.md).
- [Объяснение для защиты](docs/DEFENSE.md).

Для повторной проверки Java скачайте checkstyle-10.21.4-all.jar вне репозитория:

```text
java -jar PATH_TO_CHECKSTYLE.jar -c checkstyle.xml src tests
```

Проверяются строки до 120 символов, файлы до 1000 строк, методы до 40 строк,
до 7 параметров, сложность до 10, Javadoc, имена и MagicNumber.
В Git нет бинарников, архивов, сборки, кешей, настроек редакторов и SVG/D2-артефактов.
История технической разработки имеет нейтрального автора Variant 11 <variant11@localhost>.
У репозитория нет remote; публикация на аккаунт пользователя не выполнялась.
Получатель может указать свои данные для будущих коммитов и самостоятельно
разместить всю историю в своём публичном репозитории перед сдачей.

## Каталог функций

Ниже перечислены методы и конструкторы; подробности есть в Javadoc исходников.
Records предоставляют стандартные аксессоры полей.

| Класс | Метод | Назначение |
| --- | --- | --- |
| Config | parse | Parses pairs of option names and values, rejecting unknown or repeated keys. |
| Config | validated | Validates every setting before any HTTP request or output creation. |
| Config | repository | Checks a readable fixture path or a PyPI-compatible JSON API base URL. |
| Config | output | Checks the SVG extension, target type and nearest existing writable parent. |
| Config | settings | Shows the five user-configurable parameters for stage one only. |
| Config | root | Converts the selected package to a fixture name or fixed-version PyPI identity. |
| Csv | Csv | Prevents utility construction. |
| Csv | parse | Splits a record, accepting doubled quotes inside quoted fields. |
| Graph | build | Starts BFS, marking vertices when discovered to handle shared and cyclic edges. |
| Graph | level | Recursively processes one complete BFS layer before the next layer. |
| Graph | adjacency | Prints the stage-three adjacency list in BFS order. |
| InstallOrder | InstallOrder | Prevents utility construction. |
| InstallOrder | of | Uses Kahn's algorithm with leaves first and alphabetical tie breaking. |
| Json | Json | Stores the source document. |
| Json | parse | Parses exactly one JSON value and rejects trailing data. |
| Json | value | Reads a value with a bounded nesting depth. |
| Json | objectValue | Reads unique string keys and their values. |
| Json | arrayValue | Reads an array, preserving element order. |
| Json | string | Reads JSON string escapes and rejects unterminated strings. |
| Json | escape | Decodes a single escaped character. |
| Json | escapedSymbol | Requires the character following a backslash. |
| Json | unicode | Decodes four hexadecimal digits. |
| Json | number | Parses JSON numbers as BigDecimal, rejecting non-JSON spellings. |
| Json | literal | Reads a fixed JSON literal. |
| Json | space | Skips JSON whitespace. |
| Json | take | Consumes an optional punctuation character. |
| Json | expect | Requires a punctuation character. |
| Json | object | Requires an object and exposes its keys without unsafe generic casts. |
| Json | array | Requires an array, treating a null requires_dist value as an empty list. |
| Main | Main | Prevents utility construction. |
| Main | main | Reports errors with a nonzero exit code and no stack trace. |
| Main | execute | Validates CLI parameters and prints stage-one key-value output. |
| Markers | Markers | Tokenizes a marker expression. |
| Markers | applies | Returns whether the dependency applies to the fixed reference environment. |
| Markers | or | Parses disjunctions while consuming both operands even on a true left side. |
| Markers | and | Parses conjunctions with higher precedence than disjunctions. |
| Markers | atom | Parses parentheses or a scalar comparison. |
| Markers | evaluate | Evaluates version comparisons numerically and textual comparisons lexically. |
| Markers | textual | Evaluates a lexical comparison when operands are not both numeric versions. |
| Markers | scalar | Resolves an environment variable or removes literal quotes. |
| Markers | take | Consumes one optional token. |
| Markers | next | Requires another token. |
| PypiRepository | PypiRepository | Pins the root to the exact user-specified version. |
| PypiRepository | dependencies | Fetches version-specific Requires-Dist metadata and resolves direct edges. |
| PypiRepository | resolve | Selects the newest compatible non-yanked stable release, checking prior selections. |
| PypiRepository | available | Checks for at least one non-yanked file compatible with reference Python. |
| PypiRepository | pythonCompatible | Tests Requires-Python without invoking Python or packaging libraries. |
| PypiRepository | fetch | Downloads and caches one JSON document, preserving explicit HTTP errors. |
| Requirement | parse | Applies environment markers and rejects unsupported extras or direct URLs. |
| Requirement | normalize | Implements PyPI name normalization. |
| TestRepository | TestRepository | Loads the complete test universe and rejects dangling dependencies. |
| TestRepository | addRow | Parses a package row whose dependencies are separated by spaces. |
| TestRepository | requirePackage | Rejects packages that are absent from the fixture. |
| TestRepository | dependencies | Returns the direct edges for an existing package. |
| TestRepository | all | Returns the entire finite test repository. |
| Versions | Versions | Prevents utility construction. |
| Versions | stable | Checks whether a version belongs to the supported stable release subset. |
| Versions | compare | Compares release segments numerically with trailing zero normalization. |
| Versions | matches | Applies comma-separated constraints, accepting legacy enclosing parentheses. |
| Versions | condition | Evaluates one constraint without silently accepting unsupported syntax. |
| Versions | bounded | Evaluates an ordinary or compatible-release numeric bound. |
| Versions | wildcard | Applies the prefix matching of ==X.* and !=X.*. |
| Versions | upperCompatible | Calculates the exclusive upper bound of a compatible-release constraint. |
| Visualization | Visualization | Prevents utility construction. |
| Visualization | d2 | Includes every reachable vertex and edge, preserving cycles and isolated nodes. |
| Visualization | save | Saves D2 alongside a requested SVG, replacing the SVG only after successful rendering. |
| Visualization | render | Invokes only the renderer; dependency extraction never calls an external tool. |
| Repository | dependencies | Возвращает прямые зависимости без менеджера пакетов. |
