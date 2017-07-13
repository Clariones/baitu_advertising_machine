var Player = new function(){
    var me = this;
    var playList;
    var animationInfo = {};
    var playingStatus = {
        currentIndex : 0,
        loops : 0,
        nextTime: 0
    }
    function getCurrentTime(){
        return new Date().getTime();
    }
    function calcNextIndex(curIndx){
        var nextIdx = curIndx+1;
        if (nextIdx >= playList.length){
            nextIdx = 0;
        }
        return nextIdx;
    }

    function initImageSize(){
        var screenWidth=window.outerWidth;
        var screenHeight=window.outerHeight;
        
        console.log(screenWidth+"x"+screen.height);
        $(".v_content_image").css("width", screenWidth+"px");
        $(".v_content_image").css("height", screenHeight+"px");
    }

    function initPlayList() {
        playList = new Array();
        $('#images_list').find('div').each(function(i, item){
            var data = {
                divId: $(item).attr('id'),
                duration : $(item).data('duration')
            }
            playList.push(data);
            if (i == 0){
                $(item).show();
            }else{
                $(item).hide();
            }
        });
    }
    function doAnimation(){
        animationInfo.opacity = animationInfo.opacity + 0.1;
        if (animationInfo.opacity >= 1){
            animationInfo.imageA.css('opacity','');
            animationInfo.imageB.css('opacity','');
            animationInfo.imageA.hide();
            animationInfo.imageB.show();
            clearInterval(animationInfo.timer);
            playingStatus.currentIndex = animationInfo.newPageId;
            return;
        }
        animationInfo.imageA.show();
        animationInfo.imageB.show();
        animationInfo.imageA.css('opacity',1-animationInfo.opacity);
        animationInfo.imageB.css('opacity',animationInfo.opacity);
    }
    function switchImage(fromIdx, toIdx){
        console.log("hide " + fromIdx +" and show " + toIdx);
        
        animationInfo.newPageId = toIdx;
        animationInfo.imageA = $('#'+playList[fromIdx].divId);
        animationInfo.imageB = $('#'+playList[toIdx].divId);
        animationInfo.opacity=0;
        animationInfo.timer = setInterval(doAnimation, 100);
        
    }
    function onEvery500Ms(){
        var curTime = getCurrentTime();
        if (curTime < playingStatus.nextTime){
            return;
        }
        var nextImageIdx = calcNextIndex(playingStatus.currentIndex);
        playingStatus.nextTime =  getCurrentTime() + playList[nextImageIdx].duration*1000;
        switchImage(playingStatus.currentIndex, nextImageIdx);
    }
    function startPlaying(){
        playingStatus.nextTime = getCurrentTime() + playList[0].duration*1000;
        playingStatus.timer = setInterval(onEvery500Ms, 500);
    }
    me.doWork = function(){
        initImageSize();
        initPlayList();
        startPlaying();
    }
}