# 永城外国语课堂小功能使用统计 API


**简介**:

统计永城外国语学校（初中、高中）的课堂小功能使用情况，以导出Excel表格功能为主。

**HOST**:localhost:8081


**Version**:1.0






# 课堂小功能使用统计模块


## 数据导出


**接口地址**:`/stat/teacher/export`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`Excel文件`


**接口描述**:


**请求参数**:


|    参数名称     | 参数说明 | 请求类型  | 是否必须  |      数据类型      |
|:-----------:|:----:|:-----:|:-----:|:--------------:|
|     end     | 结束时间 | query | false |     string     |
|   gradeid   | 年级id | query | false | integer(int32) |
|    mold     | 统计类型 | query | false |     string     |
|   pageNo    |  页码  | query | false | integer(int32) |
|  pageSize   | 分页大小 | query | false | integer(int32) |
|  schoolId   | 学校id | query | true  | integer(int64) |
|    start    | 开始时间 | query | false |     string     |
| teacherName | 教师名称 | query | false |     string     |
|    xkid     | 学科id | query | false | integer(int64) |


**响应状态**:


| 状态码 |      说明      |
|-----|:------------:|
| 200 |      OK      |
| 401 | Unauthorized |
| 403 |  Forbidden   |
| 404 |  Not Found   |


**响应结果**:

导出下载“教师课堂功能统计.xlsx”


## 分页查询


**接口地址**:`/stat/teacher/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


|    参数名称     | 参数说明 | 请求类型  | 是否必须  |      数据类型      |
|:-----------:|:----:|:-----:|:-----:|:--------------:|
|     end     | 结束时间 | query | false |     string     |
|   gradeid   | 年级id | query | false | integer(int32) |
|    mold     | 统计类型 | query | false |     string     |
|   pageNo    |  页码  | query | false | integer(int32) |
|  pageSize   | 分页数  | query | false | integer(int32) |
|  schoolId   | 学校id | query | true  | integer(int64) |
|    start    | 开始时间 | query | false |     string     |
| teacherName | 教师名称 | query | false |     string     |
|    xkid     | 学科id | query | false | integer(int64) |


**响应状态**:


| 状态码 |      说明      |             schema              |
|:---:|:------------:|:-------------------------------:| 
| 200 |      OK      | Result«Page«TeacherFuncStatVO»» |
| 401 | Unauthorized |                                 |
| 403 |  Forbidden   |                                 |
| 404 |  Not Found   |                                 |


**响应参数**:


| 参数名称                                 |  参数说明  |           类型            | schema                  |
|--------------------------------------|:------:|:-----------------------:|-------------------------| 
| code                                 |        |     integer(int32)      | integer(int32)          |
| data                                 |        | Page«TeacherFuncStatVO» | Page«TeacherFuncStatVO» |
| &emsp;&emsp;countId                  |        |         string          |                         |
| &emsp;&emsp;current                  |        |     integer(int64)      |                         |
| &emsp;&emsp;maxLimit                 |        |     integer(int64)      |                         |
| &emsp;&emsp;optimizeCountSql         |        |         boolean         |                         |
| &emsp;&emsp;orders                   |        |          array          | OrderItem               |
| &emsp;&emsp;&emsp;&emsp;asc          |        |         boolean         |                         |
| &emsp;&emsp;&emsp;&emsp;column       |        |         string          |                         |
| &emsp;&emsp;pages                    |        |     integer(int64)      |                         |
| &emsp;&emsp;records                  |        |          array          | TeacherFuncStatVO       |
| &emsp;&emsp;&emsp;&emsp;className    |  班级名称  |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;duration     |   学段   |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;endTime      | 课堂结束时间 |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;realName     |  教师名称  |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;serialNumber |        |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;startTime    | 课堂开始时间 |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;subjectName  |  学科名称  |         string          |                         |
| &emsp;&emsp;&emsp;&emsp;type1Count   |  随机提问  |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type2Count   |   抢答   |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type3Count   |  限时答题  |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type4Count   |   组题   |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type5Count   |  分组教学  |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type6Count   |  拍照投影  |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type7Count   |  学生打分  |         integer         |                         |
| &emsp;&emsp;&emsp;&emsp;type8Count   |  小组打分  |         integer         |                         |
| &emsp;&emsp;searchCount              |        |         boolean         |                         |
| &emsp;&emsp;size                     |        |     integer(int64)      |                         |
| &emsp;&emsp;total                    |        |     integer(int64)      |                         |
| msg                                  |        |         string          |                         |


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"countId": "",
		"current": 0,
		"maxLimit": 0,
		"optimizeCountSql": true,
		"orders": [
			{
				"asc": true,
				"column": ""
			}
		],
		"pages": 0,
		"records": [
			{
				"className": "",
				"duration": "",
				"endTime": "",
				"realName": "",
				"serialNumber": 0,
				"startTime": "",
				"subjectName": "",
				"type1Count": 0,
				"type2Count": 0,
				"type3Count": 0,
				"type4Count": 0,
				"type5Count": 0,
				"type6Count": 0,
				"type7Count": 0,
				"type8Count": 0
			}
		],
		"searchCount": true,
		"size": 0,
		"total": 0
	},
	"msg": ""
}
```