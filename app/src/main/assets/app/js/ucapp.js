$ (function () {
	// 返回按钮
	$ ("#back").click (function () {
		ucapp.goToBack ();
	});

	// 设置按钮
	$ ("#setting").click (function () {
		ucapp.goToSetting ();
	});
	$('.distination').html(ucapp.resetting.distination);
});

var phoneType = 0;// 0浏览器 1安卓 2苹果
var ua = navigator.userAgent.toLowerCase ();
if (/iphone|ipad|ipod/.test (ua)) {
	phoneType = 2;
} else if (/android/.test (ua)) {
	phoneType = 1;
}
function JsonToString (o) {
	var arr = [];
	var fmt = function (s) {
		if (typeof s == 'object' && s != null)
			return JsonToString (s);
		if (s != undefined) {
			return /^(string|number)$/.test (typeof s) ? "\"" + s + "\"" : s;
		} else {
			return "\"\"";
		}
	}
	for ( var i in o)
		arr.push ("\"" + i + "\":" + fmt (o[i]));
	return '{' + arr.join (',') + '}';
}
function createIframe () {

	var iframe = document.createElement ('iframe');
	iframe.width = 0;
	iframe.height = 0;
	iframe.marginHeight = 0;
	iframe.marginWidth = 0;

	return iframe;
}
// 获取页面传过来的参数
function getRequest() {// 从Url获取参数
	var url = window.location.search;// 获取url中"?"符后的字串
	var theRequest = new Object();
	if (url.indexOf ("?") != -1) {
		var str = url.substr (1);
		strs = str.split ("&");
		for (var i = 0; i < strs.length; i++) {
			theRequest[strs[i].split ("=")[0]] = unescape (strs[i].split ("=")[1]);
		}
	}
	return theRequest;
}
// js 与ISO 交互方法
function sendCommand (cmd, param) {
	var url = "ucapp:" + cmd + ":" + param;
	// document.location = url;
	var iframe = createIframe ();
	iframe.src = url;
	document.body.appendChild (iframe);
	// alert(iframe.src);
}
// js 与 Android 交互对象
var ucjs = {};
if (phoneType == 1) {
	ucjs = window.ucapp || {};
}

// 统一的js调用对象
var ucapp = {
	//data : getIndex(),
	weather : getWeather(),//天气
	resetting : getResetting(),//网站设置
    callFuns : {},
    post : function (url, params, callFun) {
	    var callId = 'call_' + Math.round (Math.random () * 10000);
	    switch (phoneType) {
		    case 0:
			    $.post (url, params, function (data) {
				    callFun (data);
			    });
			    break;
		    case 1:
			    this.callFuns[callId] = callFun;
			    if (ucjs.post) {
				    ucjs.post (callId, url, JsonToString (params));
			    }
			    break;
		    case 2:
			    var objcparams = {
			        url : url,
			        params : params,
			        callId : callId
			    };
			    var objp = JsonToString (objcparams);
			    this.callFuns[callId] = callFun;
			    url = 'post:' + objp;
			    var iframe = createIframe ();
			    iframe.src = url;
			    document.body.appendChild (iframe);
			    break;
		    default:
	    }
    },
    // post回调函数
    callBack : function (callId, data) {

	    var json = JSON.parse (data);
	    this.callFuns[callId] (json);
    },
    // 定位
    location : function (callFun, reqAddr) {
	    this.callFuns['loc'] = callFun;
	    switch (phoneType) {
		    case 0:
			    ucapp.callLocBack (108.359225, 22.797809, '');
			    break;
		    case 1:
		    	if(ucjs.loc){
		    		ucjs.loc (reqAddr);
		    	}
			    break;
		    case 2:
			    sendCommand ('loc', reqAddr);
			    break;
		    default:
	    }
    },
    // 定位回调函数
    callLocBack : function (lng, lat, addr) {
    	var str ='{"lng":"'+lng+'","lat":"'+lat+'","addr":"'+addr+'"}';
    	var json = JSON.parse (str);
    	//console.log("定位后返回的数据="+str);
    	this.callFuns['loc'] (json);
    },
    // 导航
    startNavi : function (lat, lng, name) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.startNavi2) {
				    ucjs.startNavi2 (lat, lng, name);
			    }
			    break;
		    case 2:
		    	 
			    sendCommand ('startNavi', 'endLog=' + lng + "&endLat=" + lat);
			    break;
		    default:
	    }

    },
	// 导航(含起点和终点坐标)
    startNaviX : function (startLog, startLat, sName, endLog, endLat, eName) {
    	//alert("startLog:"+startLog+",startLat="+startLat +",endLog="+ endLog+",endLat="+endLat);
    	switch (phoneType) {
    	case 0:
    		break;
    	case 1:
    		if (ucjs.startNavi) {
    			ucjs.startNavi (startLog, startLat, sName, endLog, endLat, eName);
    		}
    		break;
    	case 2:
    		sendCommand ('startNavi', 'startLog=' + startLog + "&startLat=" + startLat + "&sName=" + sName + "&endLog=" + endLog + "&endLat=" + endLat + "&eName=" + eName);
    		break;
    	default:
    	}
    },
     // 解析地址获取经纬度
    getLatAndLng : function (address, callFun) {
    	this.callFuns['onGetGeoCodeResult'] = callFun;
    	switch (phoneType) {
	    	case 0:
	    		break;
	    	case 1:
	    		if (ucjs.getLatAndLng) {
	    			ucjs.getLatAndLng (address);
	    		}
	    		break;
	    	case 2:
	    		sendCommand ('getLatAndLng', 'address=' + address);
	    		break;
	    	default:
    	}
    },
     callGetLatAndLng : function (lat, lng) {
    	this.callFuns['onGetGeoCodeResult'] (lat, lng);
    },
    // 拨打电话
    call : function (phoneNum) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.call) {
				    ucjs.call (phoneNum);
			    }
			    break;
		    case 2:
			    sendCommand ('call', phoneNum);
			    break;
		    default:
	    }
    },
    // 调转登陆页面
    gotoLogin : function (url) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
		    	 ucjs.gotoLogin (url);
			    break;
		    case 2:
			    sendCommand ('gotoLogin', url);
			    break;
		    default:
	    }
    },
    // 调转页面
    gotoUrl : function (url) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
		    	ucjs.gotoUrl (url);
			    break;
		    case 2:
			    sendCommand ('gotoHome', '');
			    break;
		    default:
	    }
    },
    //检查用户登陆状态
    checkStatus : function (callFun) {
    	var callId = 'call_' + Math.round (Math.random () * 10000);
    	this.callFuns[callId] = callFun;
    	var url = getWebRoot()+"app/checkStatus.do";
	    ucjs.checkStatus(callId,url);
    },
    //保存用户名跟密码
    saveUser : function (username,password) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
		    	ucjs.saveUser (username,password);
			    break;
		    case 2:
		    	 var objp = JsonToString(params);
					sendCommand('saveUser',objp);
			    break;
		    default:
	    }
    },
    //用户注销
    logout : function () {
        switch (phoneType) {
            case 0:
                break;
            case 1:
            	ucjs.logout ();
                break;
            case 2:
                 sendCommand('logout','');
                break;
            default:
        }
    },
  //保存搜索记录
    saveSearch : function (value) {
        switch (phoneType) {
            case 0:
                break;
            case 1:
            	ucjs.saveSearch(value);
                break;
            case 2:
                 //sendCommand('logout','');
                break;
            default:
        }
    },
  //获取搜索记录
    getSearch : function (callFun) {
    	var callId = 'call_search' + Math.round (Math.random () * 10000);
    	this.callFuns[callId] = callFun;
    	switch (phoneType) {
            case 0:
                break;
            case 1:
            	ucjs.getSearch (callId);
                break;
            case 2:
                 //sendCommand('logout','');
                break;
            default:
        }
    },

    /**
	 * 检查是否收藏/喜欢
	 * 
	 * @param key
	 *            'favorites'为收藏;'hobby'为喜欢
	 * @param type
	 *            类型+ID:1酒店，2景点，3线路，4旅游产品
	 */
    hasFavorite : function (key, type, callFun) {
	    this.callFuns['hasFavorite'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.hasFavorite) {
				    ucapp.callHasFavoriteBack (ucjs.hasFavorite (key, type));
			    }
			    break;
		    case 2:
			    sendCommand ('hasFavorite', key, type);
			    break;
		    default:
	    }
    },
    // 检查是否收藏/喜欢的回调函数
    callHasFavoriteBack : function (data) {
	    // alert("data="+data);
	    // var json = eval ('(' + data + ')');
	    this.callFuns['hasFavorite'] (data);
    },
    // 添加或删除收藏/喜欢
    addOrRemoveFavorite : function (key, type, name, icon, callFun) {
	    this.callFuns['addOrRemoveFavorite'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.addOrRemoveFavorite) {
				    ucapp.callAddOrRemoveFavoriteBack (ucjs.addOrRemoveFavorite (key, type, name, icon));
			    }
			    break;
		    case 2:
                var objcparams = {
                    key : key,
                    type : type,
                    name : name,
                    icon : icon
                };
                var objp = JsonToString (objcparams);
                sendCommand ('addOrRemoveFavorite', objp);
			    break;
		    default:
	    }
    },
    // 添加或删除收藏/喜欢的回调函数
    callAddOrRemoveFavoriteBack : function (data) {
	    // var json = eval ('(' + data + ')');
	    this.callFuns['addOrRemoveFavorite'] (data);
    },
    // 获取"收藏"或"喜欢"列表
    myFavorite : function (key, callFun) {
	    this.callFuns['myFavorite'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.myFavorite) {
			    	ucapp.callMyFavoriteBack(ucjs.myFavorite (key));
			    }
			    break;
		    case 2:
			    sendCommand ('myFavorite', key);
			    break;
		    default:
	    }
    },
    // 获取"收藏"或"喜欢"列表的回调函数
    callMyFavoriteBack : function (data) {
    	  var json = eval ('(' + data + ')');
	    this.callFuns['myFavorite'] (json);
    },
    // 获取"收藏"或"喜欢"的个数
    myFavoriteCount : function (key, callFun) {
	    this.callFuns['myFavoriteCount'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.myFavoriteCount) {
			    	ucapp.callMyFavoriteCountBack(ucjs.myFavoriteCount (key));
			    }
			    break;
		    case 2:
			    sendCommand ('myFavoriteCount', key);
			    break;
		    default:
	    }
    },
    // 获取"收藏"或"喜欢"的个数的回调函数
    callMyFavoriteCountBack : function (data) {
	    this.callFuns['myFavoriteCount'] (data);
    },
    // 定位回调函数
    callLoginBack : function (success) {
	    this.callFuns['login'] (success);
    },
    // 分享应用（心仪旅行）
    createShare : function () {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.creatShare) {
				    ucjs.creatShare ();
			    }
			    break;
		    case 2:
			    sendCommand ('createShare', '');
			    break;
		    default:
	    }

    },

    // 显示提示信息
    showTip : function (msg) {
	    switch (phoneType) {
		    case 0:
			    alert (msg);
			    break;
		    case 1:
			    if (ucjs.showTip) {
				    ucjs.showTip (msg);
			    }
			    break;
		    case 2:
			    sendCommand ('showTip', msg);
			    break;
		    default:
	    }

    },
      //显示加载框
    showWatting:function(){
    switch(phoneType){
        case 0:
            
            break;
        case 1:
            if(ucjs.showWatting){
                ucjs.showWatting();
            }
            break;
        case 2:
            sendCommand('showWatting','');
            break;
        default:
    }
	},
    //隐藏加载框
	hideWatting:function(){
    switch(phoneType){
        case 0:
            break;
        case 1:
            if(ucjs.hideWatting){
                ucjs.hideWatting();
            }
            break;
        case 2:
            sendCommand('hideWatting','');
            break;
        	default:break;
    	}		
	},
    // 分享内容
    // shareChannel分享渠道:wx(微信),wxfriends(微信朋友圈),sina(新浪微博),sms(短信)
    shareWebPage : function (title, imgUrl, description, isToFriend, oAuth, url, shareChannel) {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.shareWebPage) {
				    ucjs.shareWebPage (title, imgUrl, description, isToFriend, oAuth);
			    }
			    break;
		    case 2:
			    var objcparams = {
			        shareChannel : shareChannel,
			        title : title,
			        imgUrl : imgUrl,
			        url : url,
			        desc : description
			    };
			    var objp = JsonToString (objcparams);
			    sendCommand ('shareWebPage', objp);
			    break;
		    default:
	    }
    },
    // 获取应用基本信息
    // ：版本号(verCode)，版本名称(verName),设备id(deviceId:android-->imei,ios-->设备id)
    // 百度绑定用户id(baiduUserId),绑定绑定渠道id(baiduChancelId),用户安装渠道(userChannel)
    getAppBaseInfo : function (callFun) {
	    this.callFuns['AppBaseInfo'] = callFun;
	    switch (phoneType) {
		    case 0:

			    break;
		    case 1:
			    if (ucjs.getAppBaseInfo) {
				    ucapp.callAppBaseInfoBack (ucjs.getAppBaseInfo ());
			    }
			    break;
		    case 2:
			    sendCommand ('getAppBaseInfo', '');
			    break;
		    default:
	    }

    },
    // 获取应用版本名称回调函数
    callAppBaseInfoBack : function (data) {
	    // alert("data="+data);
	    var json = eval ('(' + data + ')');
	    this.callFuns['AppBaseInfo'] (json);
    },
    // 获取应用版本名称
    getVerName : function (callFun) {
	    this.callFuns['VerName'] = callFun;
	    switch (phoneType) {
		    case 0:
			    // ucapp.callVerNameBack('1.0');
			    break;
		    case 1:
			    if (ucjs.getVerName) {
				    ucapp.callVerNameBack (ucjs.getVerName ());
			    }
			    break;
		    case 2:
			    sendCommand ('getVerName', '');
			    break;
		    default:
	    }

    },
    // 获取应用版本名称回调函数
    callVerNameBack : function (verName) {
	    this.callFuns['VerName'] (verName);
    },
    // 获取应用版本号
    getVerCode : function (callFun) {
	    this.callFuns['VerCode'] = callFun;
	    switch (phoneType) {
		    case 0:
			    // ucapp.callVerNameBack('1.0');
			    break;
		    case 1:
			    if (ucjs.getVerCode) {
				    ucapp.callVerCodeBack (ucjs.getVerCode ());
			    }
			    break;
		    case 2:
			    sendCommand ('getVerCode', '');
			    break;
		    default:
	    }

    },
    // 获取应用版本号回调函数
    callVerCodeBack : function (verCode) {
	    this.callFuns['VerCode'] (verCode);
    },
    // 调转到设置页面
    goToSetting : function () {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.goToSetting) {
				    ucjs.goToSetting ();
			    }
			    break;
		    case 2:
			    sendCommand ('goToSetting', '');
			    break;
		    default:
	    }

    },
    // 返回
    goToBack : function () {
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
		    	ucjs.goToBack();
			    break;
		    case 2:
			    sendCommand ('goToBack', '');
			    break;
		    default:
	    }

    },

    // 获取设备id
    getDeviceId : function (callFun) {
	    this.callFuns['DeviceId'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.getImei) {
				    ucapp.callDeviceIdBack (ucjs.getImei ());
			    }
			    break;
		    case 2:
			    sendCommand ('getDeviceId', '');
			    break;
		    default:
	    }

    },
    // 获取设备id回调函数
    callDeviceIdBack : function (deviceId) {
	    this.callFuns['DeviceId'] (deviceId);
    },
    // 获取百度推送userId
    getBaiduPushUserId : function (callFun) {
	    this.callFuns['BaiduPushUserId'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.getBaiduPushUserId) {
				    ucapp.callBaiduPushUserIdBack (ucjs.getBaiduPushUserId ());
			    }
			    break;
		    case 2:
			    sendCommand ('getBaiduPushUserId', '');
			    break;
		    default:
	    }

    },
    // 获取百度推送userId回调函数
    callBaiduPushUserIdBack : function (baiduPushUserId) {
	    this.callFuns['BaiduPushUserId'] (baiduPushUserId);
    },
    // 获取百度推送channelId
    getBaiduChancelId : function (callFun) {
	    this.callFuns['BaiduChancelId'] = callFun;
	    switch (phoneType) {
		    case 0:
			    break;
		    case 1:
			    if (ucjs.getBaiduChancelId) {
				    ucapp.callBaiduChancelIdBack (ucjs.getBaiduChancelId ());
			    }
			    break;
		    case 2:
			    sendCommand ('getBaiduChancelId', '');
			    break;
		    default:
	    }

    },
    // 获取百度推送channelId回调函数
    callBaiduChancelIdBack : function (channelId) {
	    this.callFuns['BaiduChancelId'] (channelId);
    },
     //微信支付
	wxpay:function(prepayid,appid,nonceStr,packageValue,partnerid,timeStamp,sign){
			switch(phoneType){
			case 0:
				//ucapp.callVerNameBack('1.0');
				break;
			case 1:
				ucjs.wxpay(prepayid,appid,nonceStr,packageValue,partnerid,timeStamp,sign);
				break;
			case 2:
				var objcparams = {
					prepayid:prepayid,
					appid:appid,
					nonceStr:nonceStr,
					packageValue:packageValue,
					partnerid:partnerid,
					timeStamp:timeStamp,
					sign:sign
		        };
		        var objp = JsonToString(objcparams);
				sendCommand('wxpay',objp);
			break;default:
			}		
	         
	}, 
    //支付宝支付
   alipay:function(repayid){
			switch(phoneType){
			case 0:
				//ucapp.callVerNameBack('1.0');
				break;
			case 1:
				ucjs.alipay(repayid);
				break;
			case 2:
				sendCommand('alipay',repayid);
			break;
			default:
			}
	         
	    },
    // 获取应用版本名称回调函数
    callWxpayBack : function (data) {
	    this.callFuns['wxpay'] (data);
    },
    // 打开一个连接
    openurl : function (url) {

	    switch (phoneType) {
		    case 0:
			    location.href = url;
			    break;
		    case 1:

			    break;
		    case 2:

			    break;
		    default:
	    }

    },
    // 检查更新
    checkUpdate : function (ver, autocheck) {
	    switch (phoneType) {
		    case 0:
			    if (!autocheck) {
				    alert ('当前已经是最新版本！');
			    }
			    break;
		    case 1:
			    if (ucjs.checkUpdate) {
				    ucjs.checkUpdate (false);
			    }
			    break;
		    case 2:
			    checkUpdateIOS (autocheck, ver);
			    break;
		    default:
	    }

    },
    scanning:function(callFun){
		this.callFuns['scan']=callFun;
		switch(phoneType){
		case 0:
			//alert("浏览器不支持此功能");
			break;
		case 1:
			ucjs.scanning();
			break;
		case 2:
			sendCommand('scanning', '');
			break;
		default:
		}
	},
	callScanBack:function(content){
		this.callFuns['scan'](content);
	},
	checkCameraUseable:function(){
		if(!ucjs.isCameraUseable()){
			showTip('请开启相机权限')
		}
	},
	openWeb:function(url){
		ucjs.openWeb(url)
	}
};

/**
 * 检查版本信息
 */
function checkUpdate () {
	var ver = $ ('#verName').html ();
	ucapp.checkUpdate (ver, false);
}
// ios检查更新调用方法:0自动检查，1手动检查
function checkUpdateIOS (autocheck, ver) {
	// alert("检查更新："+autocheck+","+ver);
	var rparams = {};
	rparams['ver'] = ver;
	$.post (webRoot + "/webAllApp/individualCenter/iosheckUpdate.do", rparams, function (data) {
		if (!autocheck) {// 手动更新
			if (data.update) {
				if (confirm ("当前版本：" + ver + "\n最新版本：" + data.newVer + "\n" + data.msg + "\n是否更新？")) {
					sendCommand ('updatexylx', 'http://app.31travel.com/fcgtravel/web/download.do');
				}
			} else {
				alert ('当前已经是最新版本！');
			}
		} else {// 自动检查更新
			if (data.update) {// 自动检查更新，只有需要更新时才提示
				if (confirm ("当前版本：" + ver + "\n最新版本：" + data.newVer + "\n" + data.msg + "\n是否更新？")) {
					sendCommand ('updatexylx', '');
				}
			}
		}
	});
}

/**
 * 显示提示信息
 */
function showTip (msg) {
	ucapp.showTip (msg);
}
/**
 * 获取根路径
 */
function getWebRoot () {
//    return 'http://192.168.31.123:8080/travel/';
//    return 'https://travel.gxucreate.com/xingan/';
	return 'xingan/';
//	return 'babu/';
//	return 'travel/';
}


function getResoucePrefix(){
//	return 'http://192.168.31.123:8080/';
	return 'https://travel.gxucreate.com/';
//	return 'http://shanglinlvyou.com/travel/';
}
/**
 * 获取图片的完整地址
 */
function getFullImageUrl (path) {
	path = path == null ? "" : path;
	var result = path;
	if (path.indexOf ('https') == -1) {
		result = getImageUrl () + path;
	}else if (path.indexOf ('http') == -1) {
		result = getImageUrl () + path;
	}
	return result;
}
/**
 * 图片根路径
 */
function getImageUrl() {
//    return 'http://192.168.31.123:8080/';
	return 'https://travel.gxucreate.com/';
//	return 'https://shanglinlvyou.com/';
}

/**
 * 验证码\头像上传路径
 */
function getCodeUrl() {

//    return 'http://192.168.31.123:8080/travel/';
    return 'https://travel.gxucreate.com/xingan/';
//    return 'https://travel.gxucreate.com/babu/';
//    return 'http://shanglinlvyou.com/travel/';

}

// 拨打电话
function callme (phone) {
	if (phone == null || phone == '') {
		phone = "110";
	}
	location = "tel:" + phone;
}

/**
 * 跳转页面
 */
function goUrl(url){
	//访问的是html页面追加一个随机数，让页面返回最新的数据
	if(url.indexOf("?")!=-1){
		if(url.indexOf("r_t_x")==-1){
			url = url+"&r_t_x="+Math.round(Math.random()*10000);
		}
	}else{
		url = url+"?r_t_x="+Math.round(Math.random()*10000);
	}
	window.location.href = url;
}

/**
 * 获取网站设置
 * @returns
 */
function getResetting() {
	var resetting = new Object()
	$.ajaxSettings.async=false;
	$.post (getCodeUrl()+'app/pre/resetting.do', {}, function (data) {
		if(data.success){
			resetting = data.reset
		}
	})
	return resetting
}

/**
 * 获取天气
 * @returns
 */
function getWeather() {
	var weather = new Object()
	$.ajaxSettings.async=false;
	$.post (getCodeUrl()+'app/pre/weather.do', {}, function (data) {
		if(data.success){
			weather = data.weather
		}
	})
	return weather
}

function getIndex(){
	$.ajaxSettings.async=false;
	var data1 = new Object()
	$.post (getCodeUrl()+'app/index.do', {}, function (data) {
		data1 = data
	})
	return data1
}

