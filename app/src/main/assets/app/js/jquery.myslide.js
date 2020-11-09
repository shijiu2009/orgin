/**
 * 
 *                       _oo0oo_
 *                      o8888888o
 *                      88" . "88
 *                      (| -_- |)
 *                      0\  =  /0
 *                    ___/`---'\___
 *                  .' \\|     |// '.
 *                 / \\|||  :  |||// \
 *                / _||||| -:- |||||- \
 *               |   | \\\  -  /// |   |
 *               | \_|  ''\---/''  |_/ |
 *               \  .-\__  '-'  ___/-. /
 *             ___'. .'  /--.--\  `. .'___
 *          ."" '<  `.___\_<|>_/___.' >' "".
 *         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *         \  \ `_.   \_ __\ /__ _/   .-` /  /
 *     =====`-.____`.___ \_____/___.-`___.-'=====
 *                       `=---='
 *
 *
 *     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *           佛祖保佑    永无BUG    永不修改
 * 需要配合jquery-mobile使用
 * @author hym
 * @date 2014-10-16
 */


(function ($) {
    $.fn.mySlide = function (options) {//options 多个参数 

    	 //默认值
        var defaultVal = {
        	interval:5000, //滚动时间间隔 0不滚动
        	direction:'left'//自动滚动方向
        };
        var opts = $.extend(defaultVal, options);
		var obj = this;
		var eleList = null;
		eleList = obj.children();
		
		var indexElement = 0;
		var intervalId = null;
		if(opts.interval>0){

			if(opts.direction == 'right'){
				intervalId = setInterval(right,opts.interval);
			}else{
				intervalId = setInterval(left,opts.interval);
			}
			
		}

		createSlide();
		
		
/******************************私有function**************************************************/
		function createSlide(){
			if(eleList.length > 0){
				obj.addClass('box');
				var firstObj = null;
				eleList.each(function(index){
					if(index == 0){ 
						$(this).addClass('list slide in'); 
						firstObj = $(this);
					}else{
						$(this).addClass('list slide out');
					}
				});
				if(firstObj){
					var width = $(window).width();
					var img = firstObj.find('img');
					var ni = new Image();
					ni.src = img.attr('src'); 
					ni.onload = function(){
						var rate = width/ni.width;
						if(rate > 1){
							rate = 1;
						}
						obj.height(ni.height*rate);
					};
				}
				if(eleList.length >0){
					var indexDiv = $('<div id="index" class="index"><i class="on"></li></div>');
					for (var i = 1; i < eleList.length; i++) {
						indexDiv.append('<i></li>');
					}
					obj.append(indexDiv);
				};
				obj.on("swipeleft",function(){
					clearInterval(intervalId);
					left();
					if(opts.interval>0){
						if(opts.direction == 'right'){
							intervalId = setInterval(right,opts.interval);
						}else{
							intervalId = setInterval(left,opts.interval);
						}
					}
					
				});  
				obj.on("swiperight",function(){
					clearInterval(intervalId);
				   	right();
					if(opts.interval>0){
						if(opts.direction == 'right'){
							intervalId = setInterval(right,opts.interval);
						}else{
							intervalId = setInterval(left,opts.interval);
						}
					}
				}); 
			}
		}
		
		function left(){
			  indexElement++;
				if (indexElement >= eleList.length) {
					indexElement = 0;
				}
				 eleList.each(function(index){
					if($(this).hasClass('out_r')){
						$(this).removeClass('out_r');
					};
					if(indexElement != index){
						$(this).addClass('out').removeClass('in_r').removeClass('in');;
					}else{
						$(this).removeClass('out');
						$(this).addClass('in');
					}
				}); 
				bottom(indexElement,eleList);
		  }
		
		  function right(){
			  indexElement--;
				if (indexElement < 0) {
					indexElement = eleList.length-1;
				}
				 eleList.each(function(index){
					if($(this).hasClass('out')){
						$(this).removeClass('out');
					};
					if(indexElement != index){
						$(this).addClass('out_r').removeClass('in_r').removeClass('in');
					}else{
						$(this).removeClass('out_r');
						$(this).addClass('in_r');
					}
				}); 
				bottom(indexElement,eleList);
		  };
		  
		  function bottom(index){
			 var eleIndex = obj.find("#index");
			var htmlIndex = '';
			eleList.each(function(i) {
				if(index == i){
					htmlIndex += '<i class="on"></i>';
				}else{
					htmlIndex += '<i></i>';	
				}
			});
			eleIndex.html(htmlIndex);
		  }
		
		//debug
        function debug(msg) {    
            if (window.console && window.console.log)    
              window.console.log(msg);    
          };    
    }
})(jQuery);  