/**
 * @author hym
 * @date 2015-7-30
 */
//TODO new Date 在firefox下无法直接格式化获取日期 ，例如new Date('2015-7-30');

(function ($) {
    $.fn.myDate = function (options) {//options 多个参数 

    	 //默认值
        var defaultVal = {
        	limitStart:'', //限制可选时间的开始时间,如果没有,开始时间为当前:'2014-09-20'
        	limitEnd:'', //限制可选时间的结束时间,如果没有,则从限制开始时间起,全部为可选 :'2014-09-22'
 //       	date:'', //初始显示日历的时间,默认当前月份:'2014-09'
        	selected:'',//初始选中的日期
        	currIndex:0,
        	priceFix:'￥',
        	bottomArray:new Array(),//可选择时间底部显示的文字,一天一个元素: ['day1','day2','day3','day4']
        	allShow:false,//底部无价格时是否显示，默认不显示
        	callback:function(data){} //回调函数
        };
        var opts = $.extend(defaultVal, options);
        var obj = this;
        var currDate = new Date();
        createDate();
		
		
/******************************function**************************************************/
		//创建日期
		function createDate(){
			var startDate = opts.limitStart==''?new Date():getDateObj(opts.limitStart);
			var endDate = null;
			if(opts.limitEnd==''){
				endDate  = new Date(startDate.getFullYear(),startDate.getMonth()+1,1);
				endDate.setMonth(endDate.getMonth()+1);
				endDate.setDate(0);
			}else{
				endDate = getDateObj(opts.limitEnd);
			}
			var currIndex = opts.currIndex;
			
			var year = currDate.getFullYear();
			var month = currDate.getMonth();
			//创建头部
			//当月
			obj.append('<div class="gldp_default"><div class="titte" ><span id="dateHeadCurr">'+year+
						'年'+(month+1)+'月</span></div>'+
						'<div class="container" id="date_container_curr"><ul class="week">'+
						'<li>日</li><li>一</li><li>二</li><li>三</li><li>四</li>'+
						'<li>五</li><li>六</li></ul><div class="clear"></div>'+
						'</div></div>');
			obj.find('#date_container_curr').append('<ul class="con" id="date_curr_'+currIndex+'"></ul>');
			//下月
			year = month+1>11?year+1:year;
			month = month+1>11?0:month+1;
			obj.append('<div class="gldp_default"><div class="titte" ><span id="dateHeadNext">'+year+
					'年'+(month+1)+'月</span></div>'+
					'<div class="container" id="date_container_next"><ul class="week">'+
					'<li>日</li><li>一</li><li>二</li><li>三</li><li>四</li>'+
					'<li>五</li><li>六</li></ul><div class="clear"></div>'+
					'</div></div>');
			
			obj.find('#date_container_next').append('<ul class="con" id="date_next_'+currIndex+'"></ul>');
			if(startDate.getTime()<=endDate.getTime()){
				var bottomArray = toDateArray(startDate,opts.bottomArray); //底部显示数据
				var selected = getDateObj(opts.selected);
				if(currDate.getTime()<=endDate.getTime()){ //结束时间在当前时间之后
					var currLi = '';
					var nextLi = '';
					var currLastDate = new Date();
					currLastDate.setMonth(currLastDate.getMonth()+1);
					currLastDate.setDate(0);
					
					//计算当月最后一天与今天相差的天数，
					//计算限制开始时间和结束时间之间相差的天数，
					//两者之差可以判断结束日期是在当月还是下月或下月之后
					var diff = diffDay(startDate,endDate)-diffDay(startDate,currLastDate);
					var startDay = 1;
					var endDay = currLastDate.getDate();
					
					
					var nextLastDate = new Date(year,month+1,1);
					nextLastDate.setDate(0);//下月的最后一天
					
					var nextStartDay = 1;
					var nextEndDay = nextLastDate.getDate();

					//创建li用到的开始日和结束日
					if(diffMonth(currDate,startDate)==0){//开始时间在当月
						if(diff>0){ //结束时间在下月
							startDay = startDate.getDate();
							if(diffMonth(nextLastDate,endDate)==0){//必须是下月
								nextEndDay =endDate.getDate();
							}
						}else if(diff <= 0){ //结束时间在当月
							startDay = startDate.getDate();
							endDay = endDate.getDate();
							
							nextStartDay = 32; 
							nextEndDay =32;
						}
					}else if(diffMonth(currDate,startDate)<0){//开始时间在当月之前
						if(diff <= 0){ //结束时间在当月
							endDay = endDate.getDate();
						}else if(diff > 0){ //结束时间在当月之后
							if(diffMonth(nextLastDate,endDate)==0){
								nextEndDay =endDate.getDate();
							}
						}
					}else{// 开始时间在下月，当月全部不可选
						startDay = 32;
						endDay = 32;
						if(diffMonth(nextLastDate,endDate)==0 || diffMonth(nextLastDate,new Date(endDate.getFullYear(),endDate.getMonth()-1))==0){//必须是下个月
							nextStartDay = startDate.getDate();
							nextEndDay = endDate.getDate();
						}else{
							nextStartDay = 32;
							nextEndDay = 32;
						}
					}
					
					//获得LI
					currLi = createDateLi(year,month-1,startDay,endDay,bottomArray,selected);// month在前面已经改变成下月的
					$('#date_curr_'+currIndex).append(currLi);
					nextLi = createDateLi(year,month,nextStartDay,nextEndDay,bottomArray,selected);
					$('#date_next_'+currIndex).append(nextLi);
				}else{ //开始时间大于结束时间，创建全部不可选的LI
					currLi = createDateLi(year,month-1,32,32,bottomArray);
					$('#date_curr_'+currIndex).append(currLi);
					nextLi = createDateLi(year,month,32,32,bottomArray);
					$('#date_next_'+currIndex).append(nextLi);
				}
			}
			
			//事件监听
			if(opts.callback){
				obj.find('.con li').each(function(){
					$(this).click(function(){
						var day = $(this).attr("day");
						if(day){
							var selected = obj.find('.first');
							selected.removeClass();
							if(selected.attr('today') == 'true'){
								selected.addClass('m');
							}
							$(this).attr('class','first');
							var currDate = getDateObj(day);
							var price = $(this).attr("price");
							opts.callback({date:currDate,text:price});
						}	
					});
				});
			}
		}
		//创建日期的LI
		function createDateLi(year,month,startDay,endDay,bottomArray,selected){
			//计算获得所有的格子
			var firstDate = new Date(year,month,1);
			var lastDate = new Date(year,month+1,0);
			var lastDay = lastDate.getDate();
			//月份1日前的空白格+（最后一天的日期 - 第一天的日期+1）/一行7个格子 ，向上取整，得到行数 ，*7 得到格子总数
			var cells = (Math.ceil((firstDate.getDay()+(lastDay-firstDate.getDate()+1))/7))*7;
			
			var li = '';
			var day = 1;
			var today = 0;
			var selectDay = 0;
			var bData = getBData(firstDate,lastDate,bottomArray);
			//判断今天是否在这个月
			if(diffMonth(currDate,firstDate) == 0){
				today = currDate.getDate();
			}
			if(selected&&diffMonth(firstDate,selected) == 0){
				selectDay = selected.getDate();
			}
			for(var cell = 0;cell<cells;cell++){
				var todayFlag = false;
				var selectFlag = false;
				if(day == today){
					todayFlag = true;
				}
				if(day == selectDay){
					selectFlag = true;
				}
				var ischoose = true;
				//TODO
				if(bData&&bData.text=='已售罄'){
					ischoose=false;
				}
				if(cell<firstDate.getDay()){
					li+=getDayLi(0,year,month,null,null,false,ischoose);//空白
				}else if(cell>=lastDay+firstDate.getDay()){ 
					li+=getDayLi(0,year,month,null,null,false,ischoose);//空白
				}else if(day>=startDay&&day<=endDay&&day>=today){
					//有日期底部数据
					if(bData&&bData.date.getDate()==day){
						
						li+=getDayLi(2,year,month,day,bData,todayFlag,selectFlag,ischoose);//可用
						bData = getBData(firstDate,lastDate,bottomArray,selectFlag,ischoose);
					}else{
						if(opts.allShow){
							li+=getDayLi(1,year,month,day,null,todayFlag,selectFlag,ischoose);//可用
						}else{
							li+=getDayLi(3,year,month,day,null,todayFlag,selectFlag,ischoose);//不可用
						}
					}
					day++;
				}else{
					li+=getDayLi(3,year,month,day,null,todayFlag,selectFlag,ischoose);//不可用
					day++;
				}
			}
			
			return li;
		}
		//获取日期底部数据
		function getBData(firstDate,lastDate,bottomArray){
			var bData = null;
			if(firstDate&&lastDate&&bottomArray != null && bottomArray.length>0){
				var obj = bottomArray.shift();
				if(obj){
					if(obj.date.getTime()>=firstDate.getTime()&&
							obj.date.getTime()<=lastDate.getTime()){
						bData = obj;
					}else{
						//不在指定日期范围内，重新装回Array中的第一位
						bottomArray.unshift(obj);
					}
				}
			}
			return bData;
		}
		
		function getDayLi(type,year,month,day,bData,today,selected,ischoose){
			var li = '';
			if(type){
				var dayStr = day;
				var classStr = '';
				var color ="";
				if(!ischoose){
					color = 'style="color: #888888"';
				}
				if(today){
					dayStr = '今天';
					if(selected){
						classStr = 'class="first" today="true"';
					}else{
						classStr = 'class="m" today="true"';
					}
				}else{
					if(selected){
						classStr = 'class="first"';
					}
				}
				
				if(type == 1){ //可用
					li ='<li day="'+year+'-'+(month+1)+'-'+day+'" '+classStr+'><a href="#"><h2>'+dayStr+'</h2></a></li>';
				}else if(type == 2){ //底部有价格，可用
					li ='<li day="'+year+'-'+(month+1)+'-'+day+'" price="'+bData.text+'" '+classStr+'><a href="#"><h2>'+dayStr+'</h2><h3 '+color+'>'+opts.priceFix+'</h3></a></li>';
				}else if(type == 3){ // 不可用
					li ='<li '+classStr+'><a href="#">'+dayStr+'</a></li>';
				}else{ //空白
					li ='<li><a href="#">&nbsp;</a></li>';
				}
			}else{ //空白
				li ='<li><a href="#">&nbsp;</a></li>';
			}
			return li;
		};
		

		/**
		 * /判断是否同一个月
		 */
		function isSameMonth(date1,date2){
			var flag = false;
			if(date1&&date2){
				if(!(date1 instanceof Date) && !(date2 instanceof Date)){
					date1 = new Date(date1);
					date2 = new Date(date2);
				}
				var year1 = date1.getFullYear();
				var year2 = date2.getFullYear();
				var month1 = date1.getMonth();
				var month2 = date2.getMonth();
				if(year1 == year2 && month1 == month2){
					flag = true;
				}
			}
			return flag;
		}
		//相差月数
		function diffMonth(date1,date2){
			var diff = 0;
			if(date1&&date2){
				date1 = getDateObj(date1);
				date2 = getDateObj(date2);
				diff = (date2.getFullYear()-date1.getFullYear())*12+(date2.getMonth()-date1.getMonth());
			}
			return diff;
		}
		//相差天数
		function diffDay(date1,date2){
			var diff = 0;
			if(date1&&date2){
				date1 = getDateObj(date1);
				date1.setHours(0);
				date1.setMinutes(0);
				date1.setSeconds(0);
				date1.setMilliseconds(0);
				date2 = getDateObj(date2);
				date2.setHours(23);
				date2.setMinutes(59);
				date2.setSeconds(59);
				date2.setMilliseconds(999);
				diff = Math.floor((date2.getTime()-date1.getTime())/(1000*3600*24));
			}
			return diff;
		}

		//转换日期字符串，排除小于今天和开始时间的数据
		function toDateArray(startDate,dateArray){
			var newArray = new Array();
			var today = new Date();
			if(startDate){
				if(diffDay(today,startDate)>0){
					today = new Date(startDate.getTime());
				};
			}
			today.setHours(0);
			today.setMinutes(0);
			today.setSeconds(0);
			today.setMilliseconds(0);
			if(dateArray&&dateArray.length>0&&currDate){
				var i = 0;
				while(i<dateArray.length){
					var dateObj = {};
					dateObj.date = getDateObj(dateArray[i].date);
					if(dateObj.date&&today.getTime()<=dateObj.date.getTime()){
						dateObj.text = dateArray[i].text;
						newArray.push(dateObj);
					}
					
					i++;
				}
			}
			return newArray.sort(sortBottom);
		}
		function sortBottom(a, b)
		{
			return a.date.getTime() - b.date.getTime();
		}
		//返回JS日期对象
		function getDateObj(date){
			if(date&&date!=''){
				if(date.constructor==Date){
					return date;
				}else if(date.constructor==String){
					date = date.replace(/-/g, "/");
					var arr = date.split("/");
					date = new Date(arr[0],arr[1]-1,arr[2]);
					return date;
				}else{
					return null;
				}
			}else{
				return null;
			}
			
		}
		//debug
        function debug(msg) {    
            if (window.console && window.console.log)    
              window.console.log(msg);    
          };    
    }
})(jQuery);  