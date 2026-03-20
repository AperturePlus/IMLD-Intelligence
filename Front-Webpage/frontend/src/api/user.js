import service from "./base";

export default {
    login(data) {
        return service({
            url: `/dj-rest-auth/login/`,
            method: "post",
            data
        })
    },
    getUserDetail() {
        return service({
            url: `/dj-rest-auth/user/`,
            method: "get"
        })
    },
    register(data) {
        return service({
            url: `/dj-rest-auth/registration/`,
            method: "post",
            data
        })
    },
}