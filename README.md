# Практическая работа №2, вариант 11

Язык реализации: Java 17+, только JDK для получения зависимостей.
Тема: граф зависимостей пакетов Python (pip).

## Этап 1

Параметры передаются опциями командной строки:
--package, --repository, --test-mode, --version, --output.
Режим --test-mode принимает строго true/false; версия - точный числовой релиз.
Выходной файл должен иметь расширение .svg. Дубликаты и неизвестные опции запрещены.

```text
run.bat --package requests --repository https://pypi.org --test-mode false --version 2.32.3 --output out.svg
```

Команда выводит пять настроек ключ=значение. Ошибки направляются в stderr,
код завершения 1, успех - 0. --stage 1..5 выбирает демонстрацию этапа.
Сборка выполняется run.bat (Windows) или sh run.sh (Linux/macOS).
Нужен JDK 17+, java и javac в PATH либо JAVA_HOME на Windows.
Исходники компилируются с --release 17 в игнорируемую папку build.
Тесты: test.bat или sh test.sh, assertions включены, JUnit не нужен.
Config.parse читает параметры, Config.settings выводит их,
Main.execute запускает приложение, Main.main обрабатывает ошибки.
Назначение каждого метода описано в Javadoc.

## Этап 2

Добавьте --stage 2 для вывода прямых зависимостей указанной версии.
PypiRepository сам обращается к /pypi/name/version/json, читает Requires-Dist
своим JSON-парсером и подбирает стабильные числовые версии из releases.
Versions проверяет ограничения, Markers вычисляет условия среды.
Эталонная среда анализа: CPython 3.12.14, Windows AMD64, без extras.
Это фиксированная модель, а не установленный на компьютере Python.
Установка Python для запуска Java-программы не нужна.
