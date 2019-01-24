/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});


    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        // 获取富文体编辑器的内容
        $scope.goods.goodsDesc.introduction = editor.html();

        /** 发送post请求 */
        baseService.sendPost("/goods/save", $scope.goods)
            .then(function(response){
                if (response.data){
                    /** 清空数据 */
                    $scope.goods = {};
                    // 清空富文体编辑器的内容
                    editor.html("");
                }else{
                    alert("操作失败！");
                }
            });
    };

    // $scope.goods.goodsDesc.itemImages = [{},{}]; 错误的
    // 定义json对象封装请求参数(初始化)
    $scope.goods = {goodsDesc : {itemImages : [], specificationItems : []}};

    // 商品的图片上传
    $scope.upload = function () {
        // 异步上传文件
        baseService.uploadFile().then(function (response) {
            // 获取响应数据 {status : 200, url : ''}
            if (response.data.status == 200){
                // {"color":"金色","url":"http://image.pinyougou.com/jd/wKgMg1qtKEOATL9nAAFti6upbx4132.jpg"}
                $scope.picEntity.url = response.data.url;
            }else{
                alert("图片上传失败！");
            }

        });
    };

    // 保存图片
    $scope.addPic = function () {
        $scope.goods.goodsDesc.itemImages.push( $scope.picEntity);
    };
    // 删除图片
    $scope.removePic = function (idx) {
        $scope.goods.goodsDesc.itemImages.splice(idx, 1);
    };

    // 根据父级id查询商品分类
    $scope.findItemCatByParentId = function (name, parentId) {
        baseService.sendGet("http://manager.pinyougou.com/itemCat/findItemCatByParentId?parentId="
            + parentId).then(function (response) {
                // 获取响应数据
                $scope[name] = response.data;
        });
    };


    // $scope.$watch() 方法可以监控$scope数据模型中变量值发生改变，可以调用事件函数
    $scope.$watch('goods.category1Id', function (newVal, oldVal) {
        //alert("新值：" + newVal + ",旧值：" + oldVal);
        // 判断newVal不是undefined
        // null 、undefined、false
        if (newVal){ // 不是undefined
            // 查询商品的二级分类
            $scope.findItemCatByParentId("itemCatList2",newVal);
        }else{
            $scope.itemCatList2 = [];
        }
    });

    // $scope.$watch() 方法可以监控二级分类id发生改变，查询三级分类
    $scope.$watch('goods.category2Id', function (newVal, oldVal) {
        //alert("新值：" + newVal + ",旧值：" + oldVal);
        // 判断newVal不是undefined
        // null 、undefined、false
        if (newVal){ // 不是undefined
            // 查询商品的三级分类
            $scope.findItemCatByParentId("itemCatList3",newVal);
        }else{
            // List<ItemCat> [{id:'',name:'',parentId:'',typeId:''},{}]
            $scope.itemCatList3 = [];
        }
    });


    // $scope.$watch() 方法可以监控三级分类id发生改变，获取类型模板id
    $scope.$watch('goods.category3Id', function (newVal, oldVal) {
        // 判断newVal不是undefined
        // null 、undefined、false
        if (newVal){ // 不是undefined
            // 获取类型模板id
            // itemCatList3: [{id:'',name:'',parentId:'',typeId:''},{}]
            for (var i = 0; i < $scope.itemCatList3.length; i++){
                // 获取一个数组元素 {id:'',name:'',parentId:'',typeId:''}
                var itemCat = $scope.itemCatList3[i];
                if (itemCat.id == newVal){
                    // 获取类型模板id
                    $scope.goods.typeTemplateId = itemCat.typeId;
                    break;
                }
            }

        }else{
            $scope.goods.typeTemplateId = null;
        }
    });


    // $scope.$watch() 方法可以监控类型模板id发生改变，获取类型模板对象
    $scope.$watch('goods.typeTemplateId', function (newVal, oldVal) {
        // 判断newVal不是undefined
        if (newVal){ // 不是undefined
            // 根据主键id查询类型模板对象 {}
            baseService.sendGet("/typeTemplate/findOne?id=" + newVal).then(function(response){
                // 获取响应数据 TypeTemplate {barndIds : [{},{}]}
                // brandIds [{"id":1,"text":"联想"},{"id":3,"text":"三星"}]
                $scope.brandIds = JSON.parse(response.data.brandIds);

                // 获取该分类对应的扩展属性
                $scope.goods.goodsDesc.customAttributeItems = JSON.parse(response.data.customAttributeItems);
            });

            // 根据类型模板id查询需要显示的规格选项数据
            baseService.sendGet("/typeTemplate/findSpecByTemplateId?id="
                + newVal).then(function(response){
                /**
                 * [{"id":27,"text":"网络","options" : [{optionName:'',id:''},{}]},
                 * {"id":32,"text":"机身内存","options" : [{},{}]}]
                 */
                // 获取响应数据
                $scope.specList = response.data;
            });
        }else{

        }
    });

    // 记录用户选中的规格选项
    $scope.updateSpecAttr = function ($event, specName, optionName) {

        /**
         * $scope.goods.goodsDesc.specificationItems 数组数据格式：
         * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
         * {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
         */
        var obj = $scope.searchJsonByKey($scope.goods.goodsDesc.specificationItems, specName);
        // obj : {"attributeValue":["联通4G"],"attributeName":"网络"}
        if (obj){ // 不是null
            // 判断checkbox是否选中
            if ($event.target.checked){ // 选中
                obj.attributeValue.push(optionName);
            }else{ // 取消选中
                // 从 obj.attributeValue删除一个数组元素
                // 获取元素在数组中的索引号 ["联通4G"]
                var idx = obj.attributeValue.indexOf(optionName);
                obj.attributeValue.splice(idx, 1);

                // {"attributeValue":[],"attributeName":"网络"}
                if (obj.attributeValue.length == 0){
                    // 从外面的数组删除里面的 {} 元素
                    idx = $scope.goods.goodsDesc.specificationItems.indexOf(obj);
                    $scope.goods.goodsDesc.specificationItems.splice(idx, 1);
                }
            }
        }else {
            $scope.goods.goodsDesc.specificationItems
                .push({"attributeValue": [optionName], "attributeName": specName});
        }

    };

    /** 从json数组中根据key查询指定的json对象 */
    $scope.searchJsonByKey = function (jsonArr, key) {
        /**
         * jsonArr:
         * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
         * {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
         *
         * key: 网络|机身内存
         */
        for (var i = 0; i < jsonArr.length; i++){
            // 取一个数组元素
            // {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
            var obj = jsonArr[i];
            // 判断attributeName这个key
            if (obj.attributeName == key){
                return obj;
            }
        }
        return null;
    };

    // 定义生成SKU列表的方法
    $scope.createItems = function () {

        /** 定义SKU数组变量，并初始化 */
        $scope.goods.items = [{spec:{}, price:0, num:9999,
            status:'0', isDefault:'0'}];

        // [{"attributeValue":["移动4G","联通3G"],"attributeName":"网络"}]
        // 获取用户选中的规格选项数组
        var specItems = $scope.goods.goodsDesc.specificationItems;

        // 循环规格选项数组
        for (var i = 0; i < specItems.length; i++){
            // 取一个数组元素
            // {"attributeValue":["移动4G","联通3G"],"attributeName":"网络"}
            var json = specItems[i];
            // 调用对SKU数组扩充的方法
            // [{"spec":{"网络":"移动3G"},"price":0,"num":9999,"status":"0","isDefault":"0"},
            // {"spec":{"网络":"移动4G"},"price":0,"num":9999,"status":"0","isDefault":"0"}]
            $scope.goods.items = $scope.swapItems($scope.goods.items,
                json.attributeName, json.attributeValue);
        }

    };

    /** 定义SKU数组扩充的方法 */
    $scope.swapItems = function (items, attributeName, attributeValue) {

        // 定义一个新的SKU数组
        var newItems = [];

        // 循环原来的SKU数组
        // [{spec:{}, price:0, num:9999,status:'0', isDefault:'0'}]
        for (var i = 0; i < items.length; i++){ // 2
            // 取一个数组元素
            // {spec:{}, price:0, num:9999,status:'0', isDefault:'0'}
            var item = items[i];
            // "attributeValue":["移动4G","联通3G"]
            // // {"attributeValue":["16G"],"attributeName":"机身内存"}
            for (var j = 0; j < attributeValue.length; j++){ // 1
                // "移动4G"
                var optionName = attributeValue[j];
                // spec: {"网络":"联通4G","机身内存":"64G"}
                // 产生一个新的SKU
                var newItem = JSON.parse(JSON.stringify(item));
                // 设置规格
                newItem.spec[attributeName] = optionName;
                // 把新的sku添加数组中
                newItems.push(newItem);
            }
        }
        return newItems;
    };


    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    // 定义状态码数组
    $scope.status = ["未审核", "已审核", "审核不通过", "关闭"];


    /** 商品上下架 */
    $scope.updateMarketable = function(status){
        if ($scope.ids.length > 0){
            // $scope.ids : [149187842867994,149187842867995,149187842867996]
            // 迭代商品的数组
            for (var i = 0; i < $scope.dataList.length; i++){
                // 取数组中的元素
                /**
                 * {
                    "category1Id": 558,
                    "auditStatus": "1",
                    "id": 149187842867992,
                    "goodsName": "OPPO A79 全面屏拍照手机"
                    }
                 *
                 */
                var obj = $scope.dataList[i];
                // 判断obj.id是不是$scope.ids数组中的元素
                if ($scope.ids.indexOf(obj.id) != -1){
                    if (obj.auditStatus != 1){ // 没有审核通过
                        alert("请选择审核通过的商品！");
                        return;
                    }
                }
            }
            // 发送异步请求
            baseService.sendGet("/goods/updateMarketable?ids="
                + $scope.ids + "&status=" + status)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                        // 清空ids
                        $scope.ids = [];
                    }else{
                        alert("操作失败！");
                    }
                });

        }else{
            alert("请选择要上下架的商品！");
        }
    };
});