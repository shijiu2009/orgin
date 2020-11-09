$(function(){
	
	// ���ذ�ť
	$("#back").click(function(){
		history.go(-1);
	});
	
	// ���ð�ť
	$("#setting").click(function(){
		location = "/lytravel/wxwap/setting/index.do";
	});
});
//����绰
function callme(phone){
	if(phone==null||phone==''){
		phone = "13768273510";
	}
	location="tel:"+phone;
}

function myAlert(options){
	var defaultVal = {
			mBackgroupColor: 'gray',
			cBackgroupColor: 'white',
			msg:''
        };
    var opts = $.extend(defaultVal, options);
    var bodyHeight = $(document.body).height();
	var bodyWidth = $(document.body).width();
    var maskerDiv = $('#maskerDiv');
    if(maskerDiv != null && maskerDiv.length>0){
    	var contentDiv = $('#alert_msg');
    	contentDiv.html(opts.msg);
    	contentDiv.css('background-color',opts.cBackgroupColor);
    	maskerDiv.css('background-color',opts.mBackgroupColor);
    	var alertDiv = $('#alertDiv');
    	alertDiv.css('left',(bodyWidth+contentDiv.width())/2);
    	$('#maskerDiv').show();
    	$('#alertDiv').show();
    }else{

    	var maskerDiv = $('<div id="maskerDiv"></div>');
    	
    	maskerDiv.css('height',bodyHeight);
    	maskerDiv.css('width',$(document.body).width());
    	maskerDiv.css('position','absolute');
    	maskerDiv.css('z-index','1998');
    	maskerDiv.css('top','0px');
    	maskerDiv.css('left','0px');
    	maskerDiv.css('opacity','0.5');
    	maskerDiv.css('background-color',opts.mBackgroupColor);
    	//������
    	var alertDiv = $('<div id="alertDiv"></div>');
    	alertDiv.css('position','absolute');
    	alertDiv.css('z-index','1999');
    	alertDiv.css('display','none');
    	alertDiv.css('top',(bodyHeight+$(document.body).offset().top)/2);
    	//����
    	var contentDiv = $('<div id="alert_msg">'+opts.msg+'</div>');
    	contentDiv.css('background-color',opts.cBackgroupColor);
    	alertDiv.append(contentDiv);
    	$('body').append(maskerDiv);
    	$('body').append(alertDiv);
    	
    	alertDiv.show();
    	alertDiv.css('left',(bodyWidth-contentDiv.width())/2);
    }
}
function closeMyAlert(){
	$('#alertDiv').hide();
	$('#maskerDiv').hide();
	
};
/**
 * ��ȡ�ַ���
 * @param source ԭ�ַ���
 * @param length ��ȡ����
 * @param ellipsize
 * */
function substring(source,length,ellipsize){
	var result = "";
	if(source!=null){
		if(source.length<=length){
			result = source;
		}else{
			var len = source.length - length;
			if(len>=2){
				if(ellipsize){
					result = source.substring(0,length)+"...";
				}else{
					result = source.substring(0,length);
				}
				
			}else{
				result = source;
			}
		}
	}
	return result;
}