## Содержание
1. [Просмотреть список доступных станций](#1)
2. [Найти нужную станцию](#2)
3. [Слушать желаемую станцию<](#3)
4. [Приостановить/продолжить воспроизведение](#4)
5. [Сохранить и записать аудио](#5)

### 1. Просмотреть список доступных станций <a name="1"></a>
При входе в приложение делается запрос на метаданные доступных станций без каких-либо критериев. При удачной обработке запроса сперва отображаюся первые 30 станций, далее по мере прокрутки ползунка подгружаются остальные станции. При возникновении ошибки приложение покажет соответствующее сообщение.

![Просмотр списка доступных станций](../Activities/Images/Activity_show.png)

### 2. Найти нужную станцию <a name="2"></a>
После нажатия кнопки поиска приложение затемняет основной экран и открывает окно поиска. Затем пользователь вводит информацию для поиска и приложение формирует и посылает соответствующий запрос. Далее приложение функционирует как в предыдущем пункте.

![Найти нужную станцию](../Activities/Images/Activity_search.png)
  
### 3. Слушать желаемую станцию<a name="3"></a>
При нажатии на иконку нужной станции приложение формирует и посылает запрос на сервер. При его удачной обработке в приложение возвращаются необходимые данные и аудио-поток начинает вопроизводиться. При возникновении ошибки приложение покажет соответствующее сообщение. 

![Слушать желаемую станцию](../Activities/Images/Activity_listening.png)

### 4. Приостановить/продолжить воспроизведение<a name="4"></a>
При нажатии кнопки "Старт/Пауза" приложение делает соответствующий запрос, при этом меняя состояние кнопки. При возникновении ошибки приложение покажет соответствующее сообщение.

![Приостановить/продолжить воспроизведение](../Activities/Images/Activity_startpause.png)

### 5. Сохранить и записать аудио<a name="5"></a>
После нажатия пользователем кнопки записи приложение посылает соответствующий запрос и начинает запись аудио-потока. Затем, когда пользователь нажимает кнопку "Остановка записи", приложение внось делает запрос и сохраняет полученных файл в память устройства. При возникновении ошибки на каком-либо из этапов приложение покажет соответствующее сообщение.

![Сохранить и записать аудио](../Activities/Images/Activity_download.png)
