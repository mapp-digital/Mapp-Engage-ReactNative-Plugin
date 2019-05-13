import React, {PureComponent,Component} from 'react';

import {
    StyleSheet,
    View,
    TouchableOpacity,
    Keyboard,
    Platform,
    Text,
} from 'react-native';


const isAndroid = Platform.OS === 'android';

/**
 * Created by Aleksandar Marinkovic on 5/16/19.
 * Copyright (c) 2019 MAPP.
 */
export default class MappButton extends PureComponent {


    handlePress = () => {
        Keyboard.dismiss();
        if(this.props.onPress())
        this.props.onPress();
    }

    render() {
        const {props} = this;

        return (
            <View style={[
                styles.wrapper,

            ]}>
                <TouchableOpacity
                    activeOpacity={0.75}
                    style={[
                        styles.container,

                    ]}
                    onPress={this.handlePress}
                >
                    <Text style={[
                        styles.text,
                       {color: 'white'}
                    ]}>

                        {props.text.toUpperCase()}
                    </Text>

                </TouchableOpacity>
            </View>
        );
    }
}


const styles = StyleSheet.create({
    wrapper: {
        marginVertical: 5,
    },
    container: {
        alignSelf: 'stretch',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: "red",
        height: 36,
        borderWidth: 0,
        borderColor: 'transparent',
        borderRadius: 2,
        flexDirection:'row',


    },
    text: {
        color: 'black',
        fontSize: 14,
        fontWeight: 'bold',
        textAlign:'center',
        paddingHorizontal: 10,
        width: "100%",
    },
    loadingContainer: {
        position: 'absolute',
        top: 0,
        bottom: 0,
        left: 0,
        right: 0,
        justifyContent: 'center',
        alignItems: 'center',
    },
    backgroundImage: {
        ...StyleSheet.absoluteFillObject,
    },
    shadow: {
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.22,
        shadowRadius: 2.22,
        elevation: 2,
    },
});
