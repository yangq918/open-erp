var saveSelectColor = {
    'Name': 'SelcetColor',
    'Color': 'theme-black'
}



// 判断用户是否已有自己选择的模板风格
if (storageLoad('SelcetColor')) {
    $('body').attr('class', storageLoad('SelcetColor').Color);

} else {
    storageSave(saveSelectColor);
    $('body').attr('class', 'theme-black');


}


// 本地缓存
function storageSave(objectData) {
    localStorage.setItem(objectData.Name, JSON.stringify(objectData));
}

function storageLoad(objectName) {
    if (localStorage.getItem(objectName)) {
        return JSON.parse(localStorage.getItem(objectName))
    } else {
        return false
    }
}

function swithModalTheme() {
    console.log(storageLoad('SelcetColor').Color);
    if("theme-white"===storageLoad('SelcetColor').Color)
    {
        $('.cus_modal_div').addClass('theme-white');
        $('.cus_modal_div').removeClass("theme-black");
    }
    else
    {
        $('.cus_modal_div').addClass('theme-black');
        $('.cus_modal_div').removeClass("theme-white");
    }
}