// 定义基础的控制器
app.controller('baseController', function ($scope) {


    // 定义分页指令需要配置信息对象
    $scope.paginationConf = {
        currentPage : 1, // 当前页码
        totalItems : 0, // 总记录数
        itemsPerPage : 10, // 页大小
        perPageOptions : [10,20,30,40,50], // 页码下拉列表框
        onChange : function () { // 监听页码改变事件
            $scope.reload();
        }
    };

    // 定义重新加载数据的方法
    $scope.reload = function () {
        // 调用分页方法
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    };


    // 定义ids数组封装多个品牌id
    $scope.ids = [];

    // 为checkbox绑定点击事件
    $scope.updateSelection = function ($event, id) {
        // $event: 事件对象 angularjs的事件对象
        // $event.target : 获取dom元素
        if ($event.target.checked){ // 选中了checkbox
            // 往数组中添加一个元素
            $scope.ids.push(id);
        }else{ // 取消选中checkbox
            // 从$scope.ids 数组中删除一个元素
            // 获取元素在数组中的索引号
            var idx = $scope.ids.indexOf(id);
            // 删除20这个元素
            // 第一个参数: 索引号
            // 第二个参数：删除的个数
            $scope.ids.splice(idx, 1);
        }
    };


    /** 提取数组中json某个属性，返回拼接的字符串(逗号分隔) */
    $scope.jsonArr2Str = function(jsonArrStr, key){
        // 把jsonArrStr转化成JSON数组对象
        // [{"id":24,"text":"欧米媞"}]
        var jsonArr = JSON.parse(jsonArrStr);
        // 定义新数组
        var resArr = [];
        // 迭代json数组
        for (var i = 0; i < jsonArr.length; i++){
            // 取数组中的一个元素 {"id":24,"text":"欧米媞"}
            var json = jsonArr[i];
            // 把json对象的值添加到新数组
            resArr.push(json[key]);
        }
        // 返回数组中的元素用逗号分隔的字符串
        return resArr.join(",");
    };


});