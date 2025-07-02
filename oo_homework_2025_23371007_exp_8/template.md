> 使用的大模型是：deepseek-r1

# 实验1输入
题目情景
一个城市正在使用公交运输管理系统（BTMS）来简化与城市相关的日常活动，以下是BTMS有关的功能介绍
BTMS负责管理城市的公交线路分配和运营，知晓所有公交车、司机、公交线路、公交站点的信息。每辆公交车具有唯一标识的车牌号、车辆类型、最大载客量等基本信息，其中车辆类型包括燃油公交车和电动公交车，燃油公交车需要记录油量，电动公交车需要记录电量。BTMS记录每位司机的姓名、性别、年龄并自动为每个司机分配一个唯一的ID。公交线路由固定的首末站、中途站点构成，并假设城市内任意两站点间的行驶路线唯一。BTMS会记录每个公交站点的名称和地理位置信息。公交线路由BTMS确定的唯一数字编号标识。公交线路编号的数字范围是0-9999，而车牌号码的长度固定为由数字和大写英文字母组成的8个字符。
BTMS会提前一年为公交路线分配公交车。每天可能会有几辆公共汽车被分配到一条路线。每辆巴士每天最多服务一条路线，但可能会在不同的日子分配到不同的路线。每条公交路线都有三个班次：早班、下午班和夜班。在特定的一天，BTMS会为特定的公交车分配一名司机。BTMS为城市工作人员提供了极大的灵活性，即公交车司机每天轮班次数没有限制，最多一天可以为公交车司机分配两个班次。
BTMS支持增删改查公交车和司机的信息。然而，由于BTMS提前一年就规划好了路线表，如果司机师傅请病假或公交车正在维修，那么就无法安排司机或为公交车分配路线，原计划的路线安排就要临时取消。
对于给定的一天，BTMS需要给出一张车次表，每一表项具备唯一ID，内容包括日期、路线编号、指定公交车的车牌号、班次、发车时间、指定司机的ID和姓名以及路线状态（正常/取消）。BTMS可以通过查询得知途径每个站点的路线编号。BTMS可以查询每个司机今日的状态以及分配的公交车和班次，也可以查询每辆公交车今日的状态和分配的司机和班次。
任务要求
按照题目情景描述的需求，设计出符合情景的类图，只需要包括类、成员属性、关系（关联、实现、泛化），可以使用大语言模型帮助生成。

格式要求：
为方便评测，需要将回答统一成以下格式：

(1) 对于每个类和属性，请固定使用对应格式：

枚举类：枚举类名(枚举项1名称, 枚举项2名称,......)

接口：interface 接口名()

一般类和成员属性：类名(属性1类型: 属性1名称, 属性2类型: 属性2名称, ......)

抽象类：abstract 类名(属性1类型: 属性1名称, 属性2类型: 属性2名称, ......)

(2) 对于每个关系，请固定使用对应格式，其中()内表示可选项，*用于表示1对多关系：

关联：类A名 associates (*)类B名，表示类A关联了类B（A的一个成员类型是B）
实现：类A名 implements 接口B名，表示类A实现了接口B
泛化：类A名 extends 类B名，表示子类A继承了父类B
(3) 提交最终答案的输出格式以json格式给出，参考如下：

{
"Enumeration": [
"Enum1(ONE, TWO, THREE)"
],
"Class": [
"Class1(int: attribute1, Class2: attribute2)",
"Class2(Enum1: attribute3, List<Class3>: attribute4)",
"Class3(int: attribute5)"
],
"Relationship": [
"Class1 associates *Class2",
"Class2 associates Enum1",
"Class2 extends Class3"
]
}
为了方便建模，以下属性类型不需要额外定义类：

基本数据类型（int, char, boolean, double等）
引用数据类型（String, List<>, Map<>等）
表示时间的数据类型（日期：LocalDate，时间：LocalTime）

# 实验1输出
根据题目情景，设计公交运输管理系统（BTMS）的类图如下：
```
json
{
"Enumeration": [
"VehicleType(FUEL, ELECTRIC)",
"ShiftType(MORNING, AFTERNOON, NIGHT)",
"RouteStatus(NORMAL, CANCELLED)"
],
"Class": [
"Bus(abstract)(String: licensePlate, VehicleType: vehicleType, int: maxCapacity)",
"FuelBus(double: fuelLevel)",
"ElectricBus(double: batteryLevel)",
"Driver(String: driverId, String: name, String: gender, int: age)",
"BusStop(String: name, String: location)",
"BusRoute(int: routeId, BusStop: startStop, BusStop: endStop, List<BusStop>: intermediateStops)",
"Schedule(String: scheduleId, LocalDate: date, BusRoute: route, Bus: bus, ShiftType: shift, LocalTime: departureTime, Driver: driver, RouteStatus: status)"
],
"Relationship": [
"BusRoute associates *BusStop",
"Schedule associates BusRoute",
"Schedule associates Bus",
"Schedule associates Driver",
"FuelBus extends Bus",
"ElectricBus extends Bus"
]
}
```
设计说明：
枚举类：

VehicleType：区分燃油/电动公交车

ShiftType：定义三个固定班次

RouteStatus：记录车次状态

核心类：

抽象类Bus：封装公交车通用属性（车牌号、类型、载客量）

FuelBus/ElectricBus：继承Bus并添加特有属性（油量/电量）

Driver：记录司机基本信息

BusStop：存储站点名称和位置

BusRoute：管理线路信息（含站点列表）

Schedule：核心业务类，记录每日车次分配（关联路线、车辆、司机）

关键关系：

公交线路与站点：BusRoute通过列表关联多个BusStop（一对多）

车次与资源：Schedule分别关联BusRoute、Bus、Driver（多对一）

车辆继承：FuelBus/ElectricBus泛化自Bus

符合题目要求：
支持车辆/司机增删改查（通过类属性实现）

线路规划：BusRoute固定站点序列

动态调度：Schedule结合日期/班次/状态实现灵活排班

异常处理：RouteStatus标记取消状态（对应维修/病假场景）

查询能力：通过关联关系实现站点路线查询、资源状态跟踪

# 实验2阶段1输入
假定你是一位java程序员，要根据情景进行程序设计
现在请你设计一个公交运输管理系统（BTMS）来简化与城市相关的日常活动
问题的背景如下：一个城市正在使用公交运输管理系统（BTMS）来简化与城市相关的日常活动，以下是BTMS有关的功能介绍
BTMS负责管理城市的公交线路分配和运营，知晓所有公交车、司机、公交线路、公交站点的信息。每辆公交车具有唯一标识的车牌号、车辆类型、最大载客量等基本信息，其中车辆类型包括燃油公交车和电动公交车，燃油公交车需要记录油量，电动公交车需要记录电量。BTMS记录每位司机的姓名、性别、年龄并自动为每个司机分配一个唯一的ID。公交线路由固定的首末站、中途站点构成，并假设城市内任意两站点间的行驶路线唯一。BTMS会记录每个公交站点的名称和地理位置信息。公交线路由BTMS确定的唯一数字编号标识。公交线路编号的数字范围是0-9999，而车牌号码的长度固定为由数字和大写英文字母组成的8个字符。
BTMS会提前一年为公交路线分配公交车。每天可能会有几辆公共汽车被分配到一条路线。每辆巴士每天最多服务一条路线，但可能会在不同的日子分配到不同的路线。每条公交路线都有三个班次：早班、下午班和夜班。在特定的一天，BTMS会为特定的公交车分配一名司机。BTMS为城市工作人员提供了极大的灵活性，即公交车司机每天轮班次数没有限制，最多一天可以为公交车司机分配两个班次。
BTMS支持增删改查公交车和司机的信息。然而，由于BTMS提前一年就规划好了路线表，如果司机师傅请病假或公交车正在维修，那么就无法安排司机或为公交车分配路线，原计划的路线安排就要临时取消。
对于给定的一天，BTMS需要给出一张车次表，每一表项具备唯一ID，内容包括日期、路线编号、指定公交车的车牌号、班次、发车时间、指定司机的ID和姓名以及路线状态（正常/取消）。BTMS可以通过查询得知途径每个站点的路线编号。BTMS可以查询每个司机今日的状态以及分配的公交车和班次，也可以查询每辆公交车今日的状态和分配的司机和班次。
请你做到：从情景中完成需求分析并抽象出类
你可以参考以下步骤：
1. 通读情景描述，关注任务当中的核心要求，识别出情景当中核心的对象
2. 按照各个对象的共性，讲其进行分类，把共性高度重合的对象抽象成一个类
3. 从描述中分析上述类应当具有的属性，你不必使用程序化的语言精确定义属性和属性的类型
4. 如果你发现存在某些属性很复杂，难以直接表示出来，请把这个属性单独当作一个类来表示
5. 检查类的设计是否满足高内聚低耦合的标准

# 实验2阶段1输出
根据公交运输管理系统（BTMS）的情景描述，我将作为Java程序员进行需求分析并抽象出类。分析过程严格遵循您提供的步骤：首先通读情景，识别核心对象；然后基于共性抽象成类；接着分析类属性，不精确定义类型；如果属性复杂，单独作为类；最后检查高内聚低耦合设计。

需求分析核心要点
核心对象识别：从描述中提取的关键实体包括公交车、司机、公交线路、公交站点、班次、路线分配、车次表条目、状态等。这些对象涉及数据管理（如增删改查）、动态分配（如公交车和司机的每日分配）、以及查询功能。

功能要求：

管理公交车（车牌号唯一、车辆类型、最大载客量）、司机（ID唯一、姓名、性别、年龄）、公交线路（编号唯一、固定站点）、公交站点（名称、位置）。

车辆类型分燃油（记录油量）和电动（记录电量），需区分处理。

路线分配提前一年规划：每天每条路线分配多辆公交车，每辆车每天最多一条路线，但可跨日变化；每条路线三个班次（早班、下午班、夜班）。

司机分配：每天为公交车分配司机，司机一天最多两个班次。

异常处理：司机病假或公交车维修时，路线状态标记为“取消”。

车次表：每日生成，条目有唯一ID、日期、路线编号、车牌号、班次、发车时间、司机ID和姓名、状态。

查询功能：查询途经站点的路线；查询司机/公交车当日状态、分配详情。

设计原则：确保类高内聚（每个类职责单一）和低耦合（类间依赖最小化，通过引用而非嵌套实现）。

抽象类设计
基于对象共性，我将核心对象抽象为以下类。每个类列出属性（属性名用驼峰命名，不指定Java类型，只描述语义；复杂属性单独成类），并说明设计理由。

Bus（公交车类）

属性：

licensePlate：唯一标识，固定8字符（数字和大写字母）。

vehicleType：车辆类型（如“燃油”或“电动”），作为枚举处理。

maxCapacity：最大载客量。

fuelLevel：油量（仅当车辆类型为燃油时有效，可为空）。

batteryLevel：电量（仅当车辆类型为电动时有效，可为空）。

设计理由：公交车是核心实体，属性包括基本信息和类型相关状态。vehicleType 作为枚举而非子类，避免过度复杂化（继承会增加耦合），油量和电量作为可空属性，简化处理。内聚性高：只关注车辆自身状态，不涉及分配逻辑。

Driver（司机类）

属性：

driverId：唯一标识，系统自动分配。

name：姓名。

gender：性别。

age：年龄。

设计理由：司机信息独立管理，属性简单。不包含动态分配数据（如当日班次），以保持低耦合（分配信息在 RouteAssignment 类中）。

BusStop（公交站点类）

属性：

stopName：站点名称。

location：地理位置信息，作为复杂属性单独成类（见 Location 类）。

设计理由：站点是路线的基础组件。位置信息复杂（需经纬度或地址），单独抽象以提升内聚。

Location（位置类，复杂属性单独成类）

属性：

latitude：纬度。

longitude：经度。

设计理由：地理位置信息无法简单用字符串表示，单独成类确保可扩展性（如未来添加海拔）。BusStop 通过引用耦合，但耦合度低。

BusRoute（公交线路类）

属性：

routeNumber：唯一标识，数字范围0-9999。

startStop：首站，引用 BusStop 对象。

endStop：末站，引用 BusStop 对象。

intermediateStops：中途站点列表，有序存储 BusStop 引用（因路线唯一，顺序重要）。

设计理由：线路由固定站点构成，属性直接引用 BusStop，避免冗余。高内聚：只管理路线静态结构，不包含动态分配（如班次），班次作为 RouteAssignment 的属性。

ShiftType（班次类型枚举，非类）

属性：定义常量值（如“早班”、“下午班”、“夜班”）。

设计理由：班次是简单分类，无需完整类，用枚举降低复杂度。在 RouteAssignment 中使用，确保类型安全。

RouteStatus（路线状态枚举，非类）

属性：定义常量值（如“正常”、“取消”）。

设计理由：状态是简单二元值，枚举比类更高效。用于 RouteAssignment。

RouteAssignment（路线分配类）

属性：

assignmentId：唯一标识，用于车次表条目。

date：分配日期。

busRoute：分配的公交线路，引用 BusRoute 对象。

bus：分配的公交车，引用 Bus 对象。

shift：班次，使用 ShiftType 枚举。

departureTime：发车时间。

driver：分配的司机，引用 Driver 对象。

status：路线状态，使用 RouteStatus 枚举（正常或取消）。

设计理由：此类代表每日动态分配的核心，对应车次表条目。属性引用其他对象（如 bus、driver），而非嵌入数据，确保低耦合（例如，查询司机姓名时通过 driver 引用获取）。高内聚：封装所有分配相关属性，包括异常状态。

BTMS（系统主类）

属性：

buses：公交车列表，存储所有 Bus 对象。

drivers：司机列表，存储所有 Driver 对象。

routes：公交线路列表，存储所有 BusRoute 对象。

stops：公交站点列表，存储所有 BusStop 对象（可从 routes 推导，但独立存储以优化查询）。

assignments：路线分配列表，存储所有 RouteAssignment 对象（按日期组织，支持提前一年规划）。

方法（仅列出关键操作，不实现）：

增删改查：addBus(), removeBus(), updateBus(), addDriver(), 等。

分配管理：assignRouteToBus()（创建 RouteAssignment），cancelAssignment()（处理病假/维修，设置状态为取消）。

车次表生成：generateScheduleForDate(date) 返回该日 RouteAssignment 列表（即车次表）。

查询功能：getRoutesByStop(stop)（返回途经站点的路线编号），getDriverStatus(driver, date)（返回司机当日分配详情），getBusStatus(bus, date)（返回公交车当日分配详情）。

设计理由：BTMS 是协调中心，管理所有对象集合和业务逻辑。高内聚：封装系统操作；低耦合：通过列表存储对象，方法参数使用引用，例如 getDriverStatus 接受 Driver 和日期，而不修改 Driver 类内部。

类间关系和整体设计
关系描述：

BTMS 聚合 Bus, Driver, BusRoute, BusStop, RouteAssignment（通过列表）。

BusRoute 引用 BusStop（startStop, endStop, intermediateStops）。

RouteAssignment 引用 BusRoute, Bus, Driver（表示动态分配）。

Bus 的属性依赖 vehicleType（燃油/电动），但未用继承以避免耦合。

枚举（ShiftType, RouteStatus）被 RouteAssignment 使用。

高内聚低耦合检查：

高内聚：每个类职责单一（如 Bus 只管理车辆属性，RouteAssignment 只处理分配），无混杂功能。例如，司机状态查询由 BTMS 处理，Driver 类不存储动态数据。

低耦合：类间通过引用交互（如 RouteAssignment 引用 Bus 而非包含其属性），减少依赖。BTMS 作为中介，封装复杂逻辑（如分配和查询），其他类独立可复用。

复杂属性处理：Location 作为单独类，因为地理位置信息具有多维属性（经纬度），不宜直接嵌入 BusStop。

未单独成类的处理：

班次（ShiftType）和状态（RouteStatus）用枚举，因它们简单且值有限。

车次表不是单独类，而是 RouteAssignment 列表的查询结果，避免冗余。

此设计满足情景需求，支持所有功能（管理、分配、查询、异常处理），并符合面向对象原则。在Java实现中，可使用集合（如List）管理对象，枚举定义常量，日期时间用java.time包处理。

# 实验2阶段2输入
假定你是一位java程序员，要根据上述情景进一步设计程序架构
你的任务是：反思并按格式输出类及属性
格式要求：
(1) 对于每个类和属性，请固定使用对应格式：
枚举类：枚举类名(枚举项1名称, 枚举项2名称,......)
接口：interface 接口名()
一般类和成员属性：类名(属性1类型: 属性1名称, 属性2类型: 属性2名称, ......)
抽象类：abstract 类名(属性1类型: 属性1名称, 属性2类型: 属性2名称, ......)
(2) 对于每个关系，请固定使用对应格式，其中()内表示可选项，*用于表示1对多关系：
关联：类A名 associates (*)类B名，表示类A关联了类B（A的一个成员类型是B）
实现：类A名 implements 接口B名，表示类A实现了接口B
泛化：类A名 extends 类B名，表示子类A继承了父类B
(3) 提交最终答案的输出格式以json格式给出，参考如下：
{
"Enumeration": [
"Enum1(ONE, TWO, THREE)"
],
"Class": [
"Class1(int: attribute1, Class2: attribute2)",
"Class2(Enum1: attribute3, List<Class3>: attribute4)",
"Class3(int: attribute5)"
],
"Relationship": [
"Class1 associates *Class2",
"Class2 associates Enum1",
"Class2 extends Class3"
]
}
为了方便建模，以下属性类型不需要额外定义类：
基本数据类型（int, char, boolean, double等）
引用数据类型（String, List<>, Map<>等）
表示时间的数据类型（日期：LocalDate，时间：LocalTime）
请你做到类的结构清晰，类和类之间独立性强，类的内部属性足够满足类的功能
你可以参考以下步骤进行检查：
1. 类的命名是否清晰？成员属性的类型是否合理？
2. 是否遗漏关键类或属性？（有没有主类？）
3. 是否构建了冗余的类或属性？
4. 定义的枚举类、抽象类、接口类是否有合理性？
# 实验2阶段2输出
```
{
    "Enumeration": [
        "VehicleType(FUEL, ELECTRIC)",
        "ShiftType(MORNING, AFTERNOON, NIGHT)",
        "RouteStatus(NORMAL, CANCELLED)"
    ],
    "Class": [
        "Bus(String: licensePlate, VehicleType: vehicleType, int: maxCapacity, double: fuelLevel, double: batteryLevel)",
        "Driver(String: driverId, String: name, String: gender, int: age)",
        "Location(double: latitude, double: longitude)",
        "BusStop(String: stopName, Location: location)",
        "BusRoute(int: routeNumber, BusStop: startStop, BusStop: endStop, List<BusStop>: intermediateStops)",
        "RouteAssignment(String: assignmentId, LocalDate: date, BusRoute: busRoute, Bus: bus, ShiftType: shift, LocalTime: departureTime, Driver: driver, RouteStatus: status)",
        "BTMS(List<Bus>: buses, List<Driver>: drivers, List<BusRoute>: routes, List<BusStop>: stops, List<RouteAssignment>: assignments)"
    ],
    "Relationship": [
        "Bus associates VehicleType",
        "BusStop associates Location",
        "BusRoute associates *BusStop",
        "RouteAssignment associates BusRoute",
        "RouteAssignment associates Bus",
        "RouteAssignment associates ShiftType",
        "RouteAssignment associates Driver",
        "RouteAssignment associates RouteStatus",
        "BTMS associates *Bus",
        "BTMS associates *Driver",
        "BTMS associates *BusRoute",
        "BTMS associates *BusStop",
        "BTMS associates *RouteAssignment"
    ]
}
```
设计说明：
枚举类设计：

VehicleType(FUEL, ELECTRIC)：明确区分燃油/电动公交车

ShiftType(MORNING, AFTERNOON, NIGHT)：定义三个固定班次

RouteStatus(NORMAL, CANCELLED)：表示路线分配状态

核心类设计：

Bus：车辆核心属性，fuelLevel和batteryLevel根据vehicleType选择性使用

Driver：司机基本信息，driverId作为唯一标识

Location：解耦地理位置信息，增强复用性

BusStop：站点信息包含名称和地理位置

BusRoute：路线结构，intermediateStops使用有序列表保证站点顺序

RouteAssignment：车次表的核心实体，聚合所有分配信息

BTMS：系统主类，通过集合管理所有实体

关系设计：

使用associates表示对象引用关系（如BusRoute引用多个BusStop）

BTMS通过*表示1对多关系（管理对象集合）

未使用继承/接口：当前场景无多态需求，保持低耦合

未冗余定义：时间类型使用标准LocalDate/LocalTime

关键设计决策：

独立位置类：Location解耦地理数据，支持未来扩展

状态驱动设计：RouteStatus直接嵌入RouteAssignment

集合管理：BTMS统一管理所有实体列表

车辆类型处理：用枚举+可选属性替代继承，避免过度复杂化

路线站点存储：List<BusStop>确保中途站点顺序性

满足需求验证：

支持增删改查：通过BTMS管理对象集合实现

分配与状态管理：RouteAssignment包含完整分配信息和状态

查询功能：BTMS可通过遍历集合实现各类查询

异常处理：RouteStatus.CANCELLED直接标记取消状态

此设计保证高内聚（每个类职责单一）和低耦合（通过引用关联），所有类均可独立扩展修改。

# 实验2阶段3输入
你是一名资深的面向对象设计专家，善于分析类之间的关系，请你结合之前的BTMS管理系统的需求和你提炼出的类，分析类之间的UML类图关系。你只需要考虑关联关系和聚合关系。
首先你需要明确：关联关系：类A名 associates (*)类B名，表示类A关联了类B（A的一个成员类型是B）；
聚合关系：用来表示集体与个体之间的关联关系，例如班级与学生之间存在聚合关系；
实现关系：类A名 implements 接口B名，表示类A实现了接口B；
泛化关系：类A名 extends 类B名，表示子类A继承了父类B；
你可以参考以下步骤完成：
step 1: 初步寻找关联关系，聚合关系，实现关系与泛化关系关系
step 2: 检查找到的关系是否存在问题
step3: 如果存在问题请在你生成的json文件基础上进行修改并完善结果
step4: 按格式要求输出结果

# 实验2阶段3输出
```
{
    "Enumeration": [
        "VehicleType(FUEL, ELECTRIC)",
        "ShiftType(MORNING, AFTERNOON, NIGHT)",
        "RouteStatus(NORMAL, CANCELLED)"
    ],
    "Class": [
        "Bus(String: licensePlate, VehicleType: vehicleType, int: maxCapacity, double: fuelLevel, double: batteryLevel)",
        "Driver(String: driverId, String: name, String: gender, int: age)",
        "Location(double: latitude, double: longitude)",
        "BusStop(String: stopName, Location: location)",
        "BusRoute(int: routeNumber, BusStop: startStop, BusStop: endStop, List<BusStop>: intermediateStops)",
        "RouteAssignment(String: assignmentId, LocalDate: date, BusRoute: busRoute, Bus: bus, ShiftType: shift, LocalTime: departureTime, Driver: driver, RouteStatus: status)",
        "BTMS(List<Bus>: buses, List<Driver>: drivers, List<BusRoute>: routes, List<BusStop>: stops, List<RouteAssignment>: assignments)"
    ],
    "Relationship": [
        // 聚合关系 (整体-部分关系)
        "BTMS aggregates *Bus",
        "BTMS aggregates *Driver",
        "BTMS aggregates *BusRoute",
        "BTMS aggregates *BusStop",
        "BTMS aggregates *RouteAssignment",
        "BusRoute aggregates *BusStop",
        
        // 关联关系 (使用关系)
        "Bus associates VehicleType",
        "BusStop associates Location",
        "RouteAssignment associates BusRoute",
        "RouteAssignment associates Bus",
        "RouteAssignment associates ShiftType",
        "RouteAssignment associates Driver",
        "RouteAssignment associates RouteStatus"
    ]
}
```
UML关系分析说明：
聚合关系 (整体-部分)：

BTMS 作为系统核心，聚合了所有业务对象：

BTMS aggregates *Bus：系统管理多辆公交车

BTMS aggregates *Driver：系统管理多名司机

BTMS aggregates *BusRoute：系统管理多条线路

BTMS aggregates *BusStop：系统管理多个站点

BTMS aggregates *RouteAssignment：系统管理多项分配

BusRoute aggregates *BusStop：线路由多个站点组成（首站/末站/中途站），站点可独立存在

关联关系 (使用关系)：

Bus associates VehicleType：公交车关联车辆类型枚举

BusStop associates Location：站点关联地理位置对象

RouteAssignment 作为核心业务对象，关联多个实体：

associates BusRoute：关联分配的线路

associates Bus：关联分配的车辆

associates Driver：关联分配的司机

associates ShiftType：关联班次类型

associates RouteStatus：关联路线状态

关键设计决策：

无冗余继承：车辆类型使用枚举而非继承，避免过度设计

合理聚合边界：

BTMS作为唯一聚合根，管理所有对象生命周期

站点被线路聚合，但可被多个线路共享

关联方向明确：

RouteAssignment单向关联业务实体，保持低耦合

枚举和值对象被单向引用

集合关系精确：

使用List<BusStop>保证站点顺序性

使用*明确表示1对多关系

此设计满足高内聚低耦合原则：

内聚性：每个类职责单一（如Bus只管理车辆属性）

耦合度：通过聚合/关联控制依赖（如BTMS仅通过引用访问对象）

可扩展性：枚举支持新增类型，聚合结构支持业务扩展