## Результаты функционального тестирования
|Cценарий|Действие|Ожидаемый результат|Фактический результат| Итог|
|:---|:---|:---|:---|:---|
|0-1: Запуск приложения без подключения к интернету | 1. Проверить подключение к интернету 2. Перейти на страницу приложения | Сообщение об отсутствии интернет-соединения | Страница отсутствует |Тест не пройден |
|1-1: Создание пользователя с уникальными данными | 1. Перейти на страницу регистрации 2. Ввод пользователем уникальных данных 3. Нажатие кнопки «Save User» | Создание нового пользователя и переход на страницу просмотра проектов | Создан новый пользователь и выполнен переход на страницу просмотра проектов |Тест пройден |
|1-2: Создание пользователя с неуникальным именем | 1. Перейти на страницу регистрации 2. Ввод пользователем неуникальных данных 3. Нажатие кнопки «Save User» | Сообщение об ошибке | Создан новый пользователь и выполнен переход на страницу просмотра проектов | Тест не пройден |
|2-1: Вход пользователя с вводом валидных данных | 1. Перейти на страницу авторизации 2. Ввод пользователем валидных данных 3. Нажатие кнопки «Login» | Переход на страницу просмотра проектов | Выполнен переход на страницу просмотра проектов | Тест пройден |
|2-2: Вход пользователя с вводом не валидных данных | 1. Перейти на страницу авторизации 2. Ввод пользователем не валидных данных 3. Нажатие кнопки «Login» | Сообщение об ошибке | Сообщение об ошибке получено | Тест пройден |
|3-1: Создание подзадачи с уникальным именем | 1. Перейти на страницу создания подзадачи 2. Ввод данных (имя уникально) 3. Нажатие кнопки «Save Task» | Создание новой подзадачи | Подзадача была создана | Тест пройден |
|3-2: Создание подзадачи с неуникальным именем |  1. Перейти на страницу создания подзадачи 2. Ввод данных (имя не уникально) 3. Нажатие кнопки «Save Task» | Сообщение об ошибке | Подзадача с неуникальным именем была создана | Тест не пройден |
|4-1: Изменение статуса подзадачи | 1. Переход на страницу информации о проекте 2. Нажатие кнопки «Completed» | Изменение статуса подзадачи | Статус подзадачи изменился | Тест пройден |
|4-2: Быстрое изменение статуса подзадачи |1. Переход на страницу информации о проекте 2. Несколько быстрых нажатий кнопки «Completed» | Быстрое изменение статуса подзадачи | Статус подзадачи несколько раз быстро изменяется | Тест пройден |
|5-1: Просмотр списка проектов | 1. Переход на страницу списка проектов | Отображение списка проектов | Список проектов отобразился | Тест пройден |
|5-2: Просмотр списка проектов при их отсутствии | 1. Переход на страницу списка проектов, при отсутствии проектов в системе | Страница списка проектов без проектов | Страница списка проектов без проектов отобразилась |Тест пройден |
|6-1: Просмотр информации о проекте | 1. Переход на страницу информации о проекте | Отображение информации о проекте | Информация о проекте отобразилась | Тест пройден |
## Замечания
* Отсутствует возможность задания статуса всему проекту;  
* Отсутствует возможность сортировки подзадач;  
* Отсутствует инструкция по работе с приложением.    
