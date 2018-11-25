
$(function () {
    // 读取body data-type 判断是哪个页面然后执行相应页面方法，方法在下面。
    var dataType = $('body').attr('data-type');
    console.log(dataType);

    //     // 判断用户是否已有自己选择的模板风格
    //    if(storageLoad('SelcetColor')){
    //      $('body').attr('class',storageLoad('SelcetColor').Color)
    //    }else{
    //        storageSave(saveSelectColor);
    //        $('body').attr('class','theme-black')
    //    }

    autoLeftNav();
    $(window).resize(function () {
        autoLeftNav();
        console.log($(window).width())
    });


    $("#logout_a").click(function () {
        logout();
    });

    //    if(storageLoad('SelcetColor')){

    //     }else{
    //       storageSave(saveSelectColor);
    //     }
})


// 风格切换

$('.tpl-skiner-toggle').on('click', function () {
    $('.tpl-skiner').toggleClass('active');
})

$('.tpl-skiner-content-bar').find('span').on('click', function () {
    $('body').attr('class', $(this).attr('data-color'))
    saveSelectColor.Color = $(this).attr('data-color');
    // 保存选择项
    storageSave(saveSelectColor);
    swithModalTheme();

})


// 侧边菜单开关


function autoLeftNav() {


    $('.tpl-header-switch-button').on('click', function () {
        if ($('.left-sidebar').is('.active')) {
            if ($(window).width() > 1024) {
                $('.tpl-content-wrapper').removeClass('active');
            }
            $('.left-sidebar').removeClass('active');
        } else {

            $('.left-sidebar').addClass('active');
            if ($(window).width() > 1024) {
                $('.tpl-content-wrapper').addClass('active');
            }
        }
    })

    if ($(window).width() < 1024) {
        $('.left-sidebar').addClass('active');
    } else {
        $('.left-sidebar').removeClass('active');
    }
}


// 侧边菜单
$('.sidebar-nav-sub-title').on('click', function () {
    $(this).siblings('.sidebar-nav-sub').slideToggle(80)
        .end()
        .find('.sidebar-nav-sub-ico').toggleClass('sidebar-nav-sub-ico-rotate');
})


function renderTable($table, url, data, $page) {
    $.post(url, data, function (result) {
        console.log(result);
        if ("0" != result.code) {
            alert(result.msg);
            return;
        }
        var datas = result.data.content;

        $table.clear();
        console.log(datas);
        $table.rows.add(datas).draw();
        $page.find(".p_total_size").text("共" + result.data.totalElements + "条," + result.data.totalPages + "页");
        if (result.data.totalElements > 0) {
            var currentPage = result.data.number;
            $page.find(".p_jump_to").removeClass("am-disabled");
            $page.find(".p_first_page").removeClass("am-disabled");
            $page.find(".p_pre_page").removeClass("am-disabled");
            $page.find(".p_next_page").removeClass("am-disabled");
            $page.find(".p_last_page").removeClass("am-disabled");
            if (currentPage == 0) {
                $page.find(".p_first_page").addClass("am-disabled");
                $page.find(".p_pre_page").addClass("am-disabled");
            }
            console.log((currentPage + 1));
            console.log(result.data.totalElements);
            if ((currentPage + 1) == result.data.totalPages) {
                $page.find(".p_next_page").addClass("am-disabled");
                $page.find(".p_last_page").addClass("am-disabled");
            }
            $page.attr("currentPage", currentPage);
            $page.attr("totalPages", result.data.totalPages);
            $page.find(".p_current_size").text(currentPage + 1);

        }
        else {
            $page.find(".p_first_page").addClass("am-disabled");
            $page.find(".p_pre_page").addClass("am-disabled");
            $page.find(".p_next_page").addClass("am-disabled");
            $page.find(".p_last_page").addClass("am-disabled");
            console.log($page.find(".p_jump_to"));
            $page.find(".p_jump_to").addClass("am-disabled");
        }
    });
}


function showSubstr(str, length) {
    if (null != str) {
        str = "";
    }
    if (str.length > length) {
        return data.substr(0, length) + "...";
    }
    return str;

}

function logout() {
    $.post("/api/user/logout", {}, function (result) {
        location.href = "/login.html";

    });
}

function showUser() {

    $.ajax({
        url: '/api/user/showUser',
        type: 'post',
        async: false,
        data: {},
        success: function (result) {
            if ("0" == result.code) {
                var user = result.data;
                console.log($("span[name='user_name_span']"));
                $("span[name='user_name_span']").text(user.userName);
                if (null != user.imgUrl && "" != user.imgUrl) {
                    $("#user_img_id").attr("src", user.imgUrl);
                }
                window.tempUserId = result.tempUserId;
                window.currentUser = user;
            }
            else {
                location.href = "/login.html";
            }
        },
        error: function () {
            location.href = "/login.html";
        }
    });
    var tempUser = window.currentUser;
    if(null!=tempUser)
    {
        if("1"==tempUser.type)
        {
            $(".only_manage_menu").hide();
            $(".only_manage").hide();
        }
        else {
            $(".only_normal").hide();
        }

    }

}


function isEndOf(str, target)
{
    if(null==str)
    {
        if(null==target)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    var start = str.length-target.length;
    if(start<0)
    {
        return false;
    }
    var arr = str.substr(start,target.length);
    if(arr == target) {
        return true;
    }
    return false;
}