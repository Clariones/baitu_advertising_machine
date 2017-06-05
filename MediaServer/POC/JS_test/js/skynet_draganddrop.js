var oid = 1000;
var currentToolId = 0;

var DragDropManager = function(){
    var showDragingPoints = true;
    var me = this;
    var controlInfo = {};
    function clearDragableElementStyle() {
        var lastElem = controlInfo.lastEffectElement;
        if (lastElem) {
            lastElem.removeClass(controlInfo.dragStyle.over_left_top_style);
            lastElem.removeClass(controlInfo.dragStyle.over_left_bottom_style);
            lastElem.removeClass(controlInfo.dragStyle.over_right_top_style);
            lastElem.removeClass(controlInfo.dragStyle.over_right_bottom_style);
        }
    }
    me.init = function(targetSelector, elementSelector, dragStyle, callback){
        controlInfo.targetSelector = targetSelector;
        controlInfo.elementSelector = elementSelector;
        controlInfo.callback = callback;
        controlInfo.dragStyle=dragStyle;
        showDragingPoints = dragStyle.showDragingPoints | false;

        $(targetSelector)[0].ondrop = me.onDrop;
        controlInfo.objId = oid++;

        callbackFun = callback;
        
        $(elementSelector).each(function(i, elem){
            $(elem).prop("draggable", true);
            //$(elem).data("index", i);
            $(elem).attr("data-index", i);
            $(elem).data("drag-drop-tool-id", controlInfo.objId);
            elem.ondragstart=me.onStart;
            elem.ondragover=me.onOver;
            //console.log("elem_"+i+": " + elem.offsetLeft+"," + elem.offsetTop+", " +$(elem).data("index"));
        });
    }
    me.onStart = function(event){
        var elemData=$(event.target).data("index");
        event.dataTransfer.setData("Text",elemData);
        currentToolId = controlInfo.objId;
        
        //console.log(elemData+" was dragged");
        controlInfo.startElement = event.target;
        controlInfo.sourceIndex = elemData;
        controlInfo.startOffsetLeft = event.target.offsetLeft + event.target.clientWidth/2;
        controlInfo.startOffsetTop = event.target.offsetTop+ event.target.clientHeight/2;
        controlInfo.startClientX = event.clientX
        controlInfo.startClientY = event.clientY;
        //$(controlInfo.startElement).hide();
        if (showDragingPoints) {
            var targetSelector = $(controlInfo.targetSelector)
            targetSelector.append('<div id="start_point" style="z-index: 100; position: absolute; height: 3px; width: 3px; color: yellow;">o</div>');
            targetSelector.append('<div id="current_point" style="z-index: 100; position: absolute; height: 3px; width: 3px; color: blue;">+</div>');
            targetSelector.append('<div id="target_point" style="z-index: 100; position: absolute; height: 3px; width: 3px; color: red;">x</div>');
        }
    }

    me.onOver = function(event){
        var tgtToolId = currentToolId;
        var orgToolId = controlInfo.objId;
        if (tgtToolId != orgToolId){
            return;
        }
        event.preventDefault();
        
        var elemData=$(event.target).data("index");
        var srcData=event.dataTransfer.getData("Text");
        
        controlInfo.targetIndex = elemData;

        var offsetLeft = event.clientX - controlInfo.startClientX + controlInfo.startOffsetLeft;
        var offsetTop = event.clientY - controlInfo.startClientY + controlInfo.startOffsetTop;

        var curElemOffsetLeft = event.target.offsetLeft;
        var curElemOffsetTop = event.target.offsetTop;
        var curElemClientWidth = event.target.clientWidth;
        var curElemClientHeight = event.target.clientHeight;

        if (showDragingPoints) {
            $('#start_point').css('left',controlInfo.startOffsetLeft+"px");
            $('#start_point').css('top',controlInfo.startOffsetTop+"px");

            $('#current_point').css('left',offsetLeft+"px");
            $('#current_point').css('top',offsetTop+"px");

            $('#target_point').css('left',curElemOffsetLeft + curElemClientWidth/2+"px");
            $('#target_point').css('top',curElemOffsetTop + curElemClientHeight/2+"px");
        }

        //console.log("offset is " +offsetLeft+","+offsetTop);

        var quadrant = "";
        if (offsetLeft < (curElemOffsetLeft + curElemClientWidth/2)){
            quadrant = "left_";
        }else{
            quadrant = "right_";
        }

        if (offsetTop < (curElemOffsetTop + curElemClientHeight/2)){
            quadrant += "top";
        }else{
            quadrant += "bottom";
        }
        controlInfo.quadrant = quadrant;
        clearDragableElementStyle();
        lastElem = $(event.target);
        lastElem.addClass(controlInfo.dragStyle["over_"+quadrant+"_style"]);
        //console.log("the source " + srcData + " is over " + elemData+" " + quadrant);
        controlInfo.lastEffectElement = lastElem;
    }
    me.onDrop = function(event){
        var tgtToolId = currentToolId;
        var orgToolId = controlInfo.objId;
        if (tgtToolId != orgToolId){
            return;
        }
        currentToolId = 0;
        clearDragableElementStyle();
        if (showDragingPoints){
            $(controlInfo.targetSelector).children("#start_point").remove();
            $(controlInfo.targetSelector).children("#current_point").remove();
            $(controlInfo.targetSelector).children("#target_point").remove();
        }
        event.dataTransfer.clearData();
        controlInfo.callback(controlInfo.sourceIndex, controlInfo.targetIndex, controlInfo.quadrant);
    }
}