//дcookies

function setCookie(name,value)
{
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString()+";path=/lytravel";
}

//��ȡcookies
function getCookie(name)
{
	
	
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
    if(arr=document.cookie.match(reg))
        return unescape(arr[2]);
    else
        return "";
       
}

//ɾ��cookies
function delCookie(name)
{
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval=getCookie(name);
    if(cval!=null)
        document.cookie= name + "="+cval+";expires="+exp.toGMTString();
} 
//-----------------------------------------�ղ�----------------------------------------
/**
 * �ղ�
 * @param title ����
 * @param icon ͼ��
 * @param type ����:1�Ƶ꣬2���㣬3��·��4���β�Ʒ
 * @param url 
 * */

function favorite(module,type,id,title,icon){
	var tmpKey = type+"_"+id;
	var tmp = tmpKey+","+type+","+id+","+title+","+icon;
	//delCookie("favorite","/fcgtravel");
	var myTmp = getCookie(module);
	
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //����һ����
		strs=myTmp.split("|"); //�ַ��ָ� 
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
 * ɾ���ҵ��ղ�
 * @param type ����:1�Ƶ꣬2���㣬3��·��4���β�Ʒ
 * @param id
 * */
function delFavorite(module,type,id){
	var tmpKey = type+"_"+id;
	var myTmp = getCookie(module);
	var tmp ="";
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //����һ����
		strs=myTmp.split("|"); //�ַ��ָ� 
		
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
 * �ж��Ƿ��ղ�
 * @param type ����:1�Ƶ꣬2���㣬3��·��4���β�Ʒ
 * @param id
 * */
function hasFavorite(module,type,id){
	var hasFavorite = false;
	var tmpKey = type+"_"+id;
	var myTmp = getCookie(module);
	
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //����һ����
		strs=myTmp.split("|"); //�ַ��ָ� 
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
 * ͳ���ղ�����
 * */
function totalFavorite(module){
	var total =0;
	var myTmp = getCookie(module);
	if(myTmp&&myTmp!=''){
		var strs= new Array(); //����һ����
		strs=myTmp.split("|"); //�ַ��ָ� 
		total = strs.length;
	}
	return total;
}
//-----------------------------------------�ղ�----------------------------------------