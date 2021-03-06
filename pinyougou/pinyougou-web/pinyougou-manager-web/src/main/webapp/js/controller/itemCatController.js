/** 定义控制器层 */
app.controller('itemCatController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/itemCat/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        var url = "save";
        if ($scope.entity.id){
            url = "update";
        }
        /** 发送post请求 */
        baseService.sendPost("/itemCat/" + url, $scope.entity)
            .then(function(response){
                if (response.data){
                    /** 重新加载数据 */
                    $scope.reload();
                }else{
                    alert("操作失败！");
                }
            });
    };

    /** 显示修改 */
    $scope.show = function(entity){
       /** 把json对象转化成一个新的json对象 */
       $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.deleteById("/itemCat/delete", $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };

    // 根据父级id查询商品分类
    $scope.findItemCatByParentId = function (parentId) {
        baseService.sendGet("/itemCat/findItemCatByParentId?parentId="
            + parentId).then(function(response){
                // 获取响应数据 List<ItemCat> [{},{}]
                $scope.dataList = response.data;
        });
    };


    // 定义分类级别的变量
    $scope.grade = 1;

    // 为查询下级按钮绑定点击事件
    $scope.selectList = function (entity, grade) {
        // entity : {name : '', id : ''}
        $scope.grade = grade;

        if (grade == 1){ // 显示一级分类
            $scope.itemCat_1 = null;
            $scope.itemCat_2 = null;
        }

        if (grade == 2) { // 显示二级分类
            // 存入$scope
            $scope.itemCat_1 = entity;
        }

        if (grade == 3) { // 显示三级分类
            // 存入$scope
            $scope.itemCat_2 = entity;
        }

        // 查询下级
        $scope.findItemCatByParentId(entity.id);
    };
});