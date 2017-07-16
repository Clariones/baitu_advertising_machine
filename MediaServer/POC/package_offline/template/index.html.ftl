<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html;charset=utf-8"/>
		<meta name="viewport" content="height=device-height, width=device-width, initial-scale=1" />
        <title>离线广告</title>
        <script src="js/jquery-1.10.2.min.js" type="text/javascript"></script>
        <script src="js/play-offline.js" type="text/javascript"></script>
        <link type="text/css" rel="stylesheet" href="css/play-offline.css" />
	</head>
    <body>
        <div id="images_list">
<#list pages as page>
            <div id="div_image${page_index}" class="playing_image" data-duration="${page.playDuration}">
                <img class="v_content_image" src="image/${page.contentId}${page.postfix}" alt="${page.imageUri}"/>
            </div>
 </#list>          
        </div>
        <div class="offline_div">
        </div>
    <script>
        window.onload = function(){
            Player.doWork();
        }
    </script>
    </body>
</html>