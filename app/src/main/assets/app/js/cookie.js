//写cookies

function setCookie(name,value)
{
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString()+";path=/lytravel";
}

//读取cookies
function getCookie(name)
{
	
	
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
    if(arr=document.cookie.match(reg))
        return unescape(arr[2]);
    else
        return "";
       
}

//删除cookies
function delCookie(name)
{
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval=getCookie(name);
    if(cval!=null)
        document.cookie= name + "="+cval+";expires="+exp.toGMTString();
} 
//-----------------------------------------收藏----------------------------------------
/**
 * 收藏
 * @param title 标题
 * @param icon 图标
 * @param type 类型:1酒店，2景点，3线路，4旅游产品
 * @param url 
 * */

function favorite(module,type,id,title,icon){
	var tmpKey = type+"_"+id;
	var tmp = tmpKey+","+type+","+id+","+title+","+icon;
	//delCookie("favorite","/fcgtravel");
	var myTmp = getCookie(module);
	
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //定义一数组
		strs=myTmp.split("|"); //字符分割 
		var nothas = true;
		for (var i=0;i<strs.length ;i++ ){
			if(strs[i].indexOf(tmpKey)>=0){
				nothas = false;
				break;
			}
		} 
		if(nothas){
			myTmp =myTmp+"|"+tmp;
		}
	}else{
		myTmp =tmp;
	}
	
	setCookie(module,myTmp);
}
/**
 * 删除我的收藏
 * @param type 类型:1酒店，2景点，3线路，4旅游产品
 * @param id
 * */
function delFavorite(module,type,id){
	var tmpKey = type+"_"+id;
	var myTmp = getCookie(module);
	var tmp ="";
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //定义一数组
		strs=myTmp.split("|"); //字符分割 
		
		for (var i=0;i<strs.length ;i++ ){
			if(strs[i].length>0&&strs[i].indexOf(tmpKey)<0){
				tmp += strs[i]+"|";
			}
		} 
		if(tmp.length>1){
			tmp = tmp.substring(0,tmp.length-1);
		}
	}
	
	setCookie(module,tmp);
	return true;
}
/**
 * 判断是否收藏
 * @param type 类型:1酒店，2景点，3线路，4旅游产品
 * @param id
 * */
function hasFavorite(module,type,id){
	var hasFavorite = false;
	var tmpKey = type+"_"+id;
	var myTmp = getCookie(module);
	
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //定义一数组
		strs=myTmp.split("|"); //字符分割 
		for (var i=0;i<strs.length ;i++ ){
			if(strs[i].indexOf(tmpKey)>=0){
				hasFavorite = true;
				break;
			}
		} 
		
	}
	
	return hasFavorite;
}
/**
 * 统计收藏数量
 * */
function totalFavorite(module){
	var total =0;
	var myTmp = getCookie(module);
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //定义一数组
		strs=myTmp.split("|"); //字符分割 
		total = strs.length;
	}
	return total;
}
//-----------------------------------------收藏----------------------------------------